package io.github.arenoros.anime365.core.player

import android.content.Context
import android.net.ConnectivityManager
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Requirements
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.arenoros.anime365.core.datastore.SettingsStore
import io.github.arenoros.anime365.core.model.DownloadState
import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.EpisodeDownload
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.VideoStream
import io.github.arenoros.anime365.core.model.pickHeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

/** Скачивание Переводов (docs/roadmap.md, M4). Интерфейс — чтобы ViewModel проверялись без Media3. */
interface EpisodeDownloads {
    /** Все Скачивания по порядку постановки; пока что-то качается — обновляется раз в секунду. */
    val downloads: Flow<List<EpisodeDownload>>

    /**
     * В очередь — в Разрешении из Настроек или ближайшем меньшем; Отдельные субтитры — вместе с видео.
     * Уже скачанный или скачиваемый Перевод не ставится повторно.
     */
    suspend fun download(
        series: SeriesSummary,
        episode: Episode,
        translation: Translation,
        stream: VideoStream,
    )

    /** Скачивание с ошибкой — заново. */
    fun retry(translationId: Int)

    fun remove(translationIds: Collection<Int>)

    /** Продолжить незаконченное, когда приложение снова открыли. */
    fun resume()
}

/** Ключ кэша, по которому Плеер находит скачанное видео ([PlayerMedia.cacheKey]). */
val EpisodeDownload.cacheKey: String get() = DOWNLOAD_KEY_PREFIX + translation.id

private const val PROGRESS_INTERVAL_MS = 1_000L
private const val VIDEO_ID = "video:"
private const val SUBTITLES_ID = "subtitles:"

@OptIn(UnstableApi::class)
@Singleton
internal class MediaDownloads
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val storage: DownloadStorage,
        private val settings: SettingsStore,
    ) : EpisodeDownloads {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

        /** Создаётся в главном потоке при первом обращении: его колбэки приходят в тот же поток. */
        val manager: DownloadManager by lazy {
            DownloadManager(
                context,
                storage.databaseProvider,
                storage.cache,
                storage.network,
                Executors.newFixedThreadPool(DOWNLOAD_THREADS),
            ).also { manager ->
                manager.maxParallelDownloads = DOWNLOAD_THREADS
                // «Только по Wi-Fi» можно переключить, пока что-то качается.
                scope.launch {
                    settings.settings.map { it.downloadWifiOnly }.distinctUntilChanged().collect { wifiOnly ->
                        manager.requirements =
                            Requirements(if (wifiOnly) Requirements.NETWORK_UNMETERED else Requirements.NETWORK)
                    }
                }
            }
        }

        @kotlin.OptIn(ExperimentalCoroutinesApi::class)
        override val downloads: Flow<List<EpisodeDownload>> =
            callbackFlow {
                val listener =
                    object : DownloadManager.Listener {
                        override fun onInitialized(downloadManager: DownloadManager) {
                            trySend(Unit)
                        }

                        override fun onDownloadChanged(
                            downloadManager: DownloadManager,
                            download: Download,
                            finalException: Exception?,
                        ) {
                            trySend(Unit)
                        }

                        override fun onDownloadRemoved(
                            downloadManager: DownloadManager,
                            download: Download,
                        ) {
                            trySend(Unit)
                        }

                        override fun onRequirementsStateChanged(
                            downloadManager: DownloadManager,
                            requirements: Requirements,
                            notMetRequirements: Int,
                        ) {
                            trySend(Unit)
                        }
                    }
                manager.addListener(listener)
                trySend(Unit)
                awaitClose { manager.removeListener(listener) }
            }.flowOn(Dispatchers.Main.immediate)
                .conflate()
                .transformLatest {
                    // Прогресс Media3 не присылает: пока что-то качается, перечитываем сами.
                    do {
                        val snapshot = snapshot()
                        emit(snapshot)
                        val active = snapshot.any { it.state == DownloadState.Downloading }
                        if (active) delay(PROGRESS_INTERVAL_MS)
                    } while (active)
                }

        override suspend fun download(
            series: SeriesSummary,
            episode: Episode,
            translation: Translation,
            stream: VideoStream,
        ) {
            val target = settings.settings.first().downloadHeight
            val height = pickHeight(stream.downloads.map { it.height }, target) ?: return
            val existing = stored(VIDEO_ID + translation.id)
            if (existing != null && existing.state != Download.STATE_FAILED) return
            val record =
                DownloadRecord.of(series, episode, translation, height, stream.subtitlesUrl)
            val video =
                DownloadRequest
                    .Builder(
                        VIDEO_ID + translation.id,
                        stream.downloads
                            .first { it.height == height }
                            .url
                            .toUri(),
                    ).setCustomCacheKey(DOWNLOAD_KEY_PREFIX + translation.id)
                    .setData(record.encode())
                    .build()
            send(video)
            stream.subtitlesUrl?.let { url ->
                send(DownloadRequest.Builder(SUBTITLES_ID + translation.id, url.toUri()).build())
            }
        }

        override fun retry(translationId: Int) {
            scope.launch {
                val failed =
                    stored(VIDEO_ID + translationId)?.takeIf { it.state == Download.STATE_FAILED } ?: return@launch
                send(failed.request)
            }
        }

        override fun remove(translationIds: Collection<Int>) {
            translationIds.forEach { id ->
                listOf(VIDEO_ID, SUBTITLES_ID).forEach { prefix ->
                    DownloadService.sendRemoveDownload(context, AnimeDownloadService::class.java, prefix + id, false)
                }
            }
        }

        override fun resume() {
            // Сервис держит процесс и показывает уведомление; нечего качать — остановится сам.
            DownloadService.start(context, AnimeDownloadService::class.java)
        }

        private fun send(request: DownloadRequest) {
            DownloadService.sendAddDownload(context, AnimeDownloadService::class.java, request, false)
        }

        /** Менеджер живёт в главном потоке, его база читается в фоне. */
        private suspend fun stored(id: String): Download? {
            val index = withContext(Dispatchers.Main.immediate) { manager.downloadIndex }
            return withContext(Dispatchers.IO) { index.getDownload(id) }
        }

        /** База закачек — для всех, текущие закачки — для свежего прогресса (база пишется реже). */
        private suspend fun snapshot(): List<EpisodeDownload> {
            val index = withContext(Dispatchers.Main.immediate) { manager.downloadIndex }
            val stored =
                withContext(Dispatchers.IO) {
                    index.getDownloads().use { cursor ->
                        buildList { while (cursor.moveToNext()) add(cursor.download) }
                    }
                }
            return withContext(Dispatchers.Main.immediate) {
                val current = manager.currentDownloads.associateBy { it.request.id }
                val waiting = waitingState()
                stored
                    .map { current[it.request.id] ?: it }
                    .filter { it.request.id.startsWith(VIDEO_ID) && it.state != Download.STATE_REMOVING }
                    .sortedBy { it.startTimeMs }
                    .mapNotNull { it.toModel(waiting) }
            }
        }

        /** Чего ждёт очередь: Wi-Fi, если включено «Только по Wi-Fi» и сеть есть, иначе сети. */
        private fun waitingState(): DownloadState? {
            if (manager.notMetRequirements == 0) return null
            val network = context.getSystemService(ConnectivityManager::class.java)?.activeNetwork
            return if (manager.requirements.isUnmeteredNetworkRequired && network != null) {
                DownloadState.WaitingForWifi
            } else {
                DownloadState.WaitingForNetwork
            }
        }

        private fun Download.toModel(waiting: DownloadState?): EpisodeDownload? {
            val record = DownloadRecord.decode(request.data) ?: return null
            val state =
                when (state) {
                    Download.STATE_DOWNLOADING -> DownloadState.Downloading
                    Download.STATE_COMPLETED -> DownloadState.Completed
                    Download.STATE_FAILED -> DownloadState.Failed
                    else -> waiting ?: DownloadState.Queued
                }
            return record.toModel(
                videoUrl = request.uri.toString(),
                state = state,
                bytesDownloaded = bytesDownloaded,
                contentLength = contentLength.takeIf { it != C.LENGTH_UNSET.toLong() },
            )
        }

        private companion object {
            // Две серии сразу, остальные ждут в очереди.
            const val DOWNLOAD_THREADS = 2
        }
    }
