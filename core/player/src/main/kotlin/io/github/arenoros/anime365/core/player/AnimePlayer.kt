package io.github.arenoros.anime365.core.player

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.SubtitleView
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.peerless2012.ass.media.AssHandler
import io.github.peerless2012.ass.media.kt.withAssSupport
import io.github.peerless2012.ass.media.parser.AssSubtitleParserFactory
import io.github.peerless2012.ass.media.type.AssRenderType
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Видеопоток одного Перевода и подписи для уведомления: [subtitle] — «7 серия · AniLibria».
 * [hasPreviousEpisode] и [hasNextEpisode] — показывать ли в уведомлении ⏮ и ⏭.
 * [cacheKey] — у скачанного ([EpisodeDownload.cacheKey]): видео читается из кэша без сети.
 */
data class PlayerMedia(
    val videoUrl: String,
    val subtitlesUrl: String?,
    val title: String,
    val subtitle: String,
    val artworkUrl: String?,
    val hasPreviousEpisode: Boolean = false,
    val hasNextEpisode: Boolean = false,
    val cacheKey: String? = null,
)

private const val SEEK_INCREMENT_MS = 10_000L

// Id внешней дорожки должен не совпадать с дорожками самого файла (README lib_ass_media).
private const val SUBTITLES_TRACK_ID = "200"

/**
 * Один плеер на процесс: его делят экран Плеера и [PlaybackService] — фоновый звук, уведомление,
 * гарнитура. Отдельные субтитры рисует libass поверх видео (ADR 0008).
 */
@OptIn(UnstableApi::class)
@Singleton
class AnimePlayer
    @Inject
    internal constructor(
        @param:ApplicationContext private val context: Context,
        private val storage: DownloadStorage,
    ) : VideoPlayer {
        // OVERLAY_OPEN_GL: анимации и надписи с покадровым трекингом, рисует не в UI-потоке.
        private val assHandler = AssHandler(AssRenderType.OVERLAY_OPEN_GL)
        private var serviceStarted = false
        private var session = 0
        private val endedEvents =
            MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        private val skipEvents =
            MutableSharedFlow<EpisodeSkip>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

        /** Создаётся при первом запуске Плеера: до него ни кодеки, ни libass не нужны. */
        override val player: Player by lazy(::build)

        override val positionMs: Long get() = player.currentPosition

        private val playingState = MutableStateFlow(false)
        override val playing: StateFlow<Boolean> = playingState

        override val durationMs: Long get() = player.duration.coerceAtLeast(0)

        override val ended: Flow<Unit> = endedEvents

        override val episodeSkips: Flow<EpisodeSkip> = skipEvents

        /** Что играет сейчас — для кнопок уведомления. */
        internal val currentMedia: PlayerMedia?
            get() = player.currentMediaItem?.localConfiguration?.tag as? PlayerMedia

        override val isMeteredNetwork: Boolean
            get() = context.getSystemService(ConnectivityManager::class.java)?.isActiveNetworkMetered != false

        override var speed: Float
            get() = player.playbackParameters.speed
            set(value) = player.setPlaybackSpeed(value)

        override fun play(
            media: PlayerMedia,
            startPositionMs: Long,
        ): Int {
            player.setMediaItem(media.toMediaItem(), startPositionMs)
            player.prepare()
            player.play()
            if (!serviceStarted) {
                context.startService(Intent(context, PlaybackService::class.java))
                serviceStarted = true
            }
            return ++session
        }

        /** Другое Разрешение того же Перевода: позиция и пауза сохраняются (docs/screens.md). */
        override fun switchVideo(media: PlayerMedia) {
            val position = player.currentPosition
            val playWhenReady = player.playWhenReady
            player.setMediaItem(media.toMediaItem(), position)
            player.prepare()
            player.playWhenReady = playWhenReady
        }

        override var subtitlesEnabled: Boolean
            get() = C.TRACK_TYPE_TEXT !in player.trackSelectionParameters.disabledTrackTypes
            set(enabled) {
                player.trackSelectionParameters =
                    player.trackSelectionParameters
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !enabled)
                        .build()
            }

        override fun stop(session: Int) {
            if (session != this.session) return
            player.stop()
            player.clearMediaItems()
            context.stopService(Intent(context, PlaybackService::class.java))
            serviceStarted = false
        }

        /** libass рисует в overlay внутри [SubtitleView] экрана; у каждого нового экрана — свой. */
        internal fun attach(subtitleView: SubtitleView) = subtitleView.withAssSupport(assHandler)

        internal fun skip(direction: EpisodeSkip) {
            skipEvents.tryEmit(direction)
        }

        internal fun onServiceDestroyed() {
            serviceStarted = false
        }

        private fun build(): ExoPlayer {
            // Скачанное — из кэша Скачивания, остальное — из сети (DownloadStorage).
            val dataSource = storage.playback
            val assParsers = AssSubtitleParserFactory(assHandler)
            val player =
                ExoPlayer
                    .Builder(context)
                    // Внешние субтитры разбираются фабрикой парсеров, а не экстрактором.
                    .setMediaSourceFactory(DefaultMediaSourceFactory(dataSource).setSubtitleParserFactory(assParsers))
                    .setRenderersFactory(DefaultRenderersFactory(context).withAssSupport(assHandler))
                    .setAudioAttributes(
                        AudioAttributes
                            .Builder()
                            .setUsage(C.USAGE_MEDIA)
                            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                            .build(),
                        true,
                    ).setHandleAudioBecomingNoisy(true)
                    .setWakeMode(C.WAKE_MODE_NETWORK)
                    .setSeekBackIncrementMs(SEEK_INCREMENT_MS)
                    .setSeekForwardIncrementMs(SEEK_INCREMENT_MS)
                    .build()
            assHandler.init(player)
            player.addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) endedEvents.tryEmit(Unit)
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        playingState.value = isPlaying
                    }
                },
            )
            return player
        }
    }

@OptIn(UnstableApi::class)
private fun PlayerMedia.toMediaItem(): MediaItem {
    val subtitles =
        subtitlesUrl?.let { url ->
            // Тип задаём явно: часть файлов приходит как text/html (docs/api-notes.md).
            MediaItem.SubtitleConfiguration
                .Builder(url.toUri())
                .setMimeType(MimeTypes.TEXT_SSA)
                .setId(SUBTITLES_TRACK_ID)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
        }
    return MediaItem
        .Builder()
        .setUri(videoUrl)
        .setCustomCacheKey(cacheKey)
        .setTag(this)
        .setSubtitleConfigurations(listOfNotNull(subtitles))
        .setMediaMetadata(
            MediaMetadata
                .Builder()
                .setTitle(title)
                .setArtist(subtitle)
                .setArtworkUri(artworkUrl?.toUri())
                .build(),
        ).build()
}
