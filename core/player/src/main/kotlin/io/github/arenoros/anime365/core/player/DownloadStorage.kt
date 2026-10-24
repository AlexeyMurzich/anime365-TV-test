package io.github.arenoros.anime365.core.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.arenoros.anime365.core.datastore.AccountStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Ключ кэша скачанного видео: по нему Плеер находит файл без сети, какой бы ни была ссылка. */
internal const val DOWNLOAD_KEY_PREFIX = "download:"

/**
 * Хранилище Скачивания, общее для загрузчика и Плеера: Плеер читает скачанное из того же кэша.
 * Файлы — в `getExternalFilesDir` (docs/roadmap.md, M4): удаляются вместе с приложением и не входят
 * в резервную копию (data_extraction_rules.xml).
 */
@OptIn(UnstableApi::class)
@Singleton
internal class DownloadStorage
    @Inject
    constructor(
        @ApplicationContext context: Context,
        client: OkHttpClient,
        private val accounts: AccountStore,
    ) {
        val databaseProvider = StandaloneDatabaseProvider(context)

        val cache =
            SimpleCache(
                File(context.getExternalFilesDir(null) ?: context.filesDir, DIRECTORY),
                NoOpCacheEvictor(),
                databaseProvider,
            )

        /** Сеть: субтитры распаковываются, ссылкам Скачивания подставляется Токен. */
        val network: DataSource.Factory =
            ResolvingDataSource.Factory(
                OkHttpDataSource.Factory(client.newBuilder().addNetworkInterceptor(GzipInterceptor).build()),
            ) { it.withAccessToken() }

        /** Для Плеера: скачанное — из кэша, остальное — из сети без записи в кэш. */
        val playback: DataSource.Factory =
            CacheDataSource
                .Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(network)
                .setCacheWriteDataSinkFactory(null)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        /**
         * Токен — только ссылкам Скачивания и только в момент запроса: в базе закачек его нет (docs/roadmap.md,
         * M4). Ссылки Скачивания узнаются по ключу кэша. Загрузчик ходит в сеть в своём потоке — ждать можно.
         */
        private fun DataSpec.withAccessToken(): DataSpec {
            val token =
                if (key?.startsWith(DOWNLOAD_KEY_PREFIX) == true) {
                    runBlocking { accounts.account.first()?.accessToken }
                } else {
                    null
                }
            return if (token ==
                null
            ) {
                this
            } else {
                withUri(uri.buildUpon().appendQueryParameter("access_token", token).build())
            }
        }

        private companion object {
            const val DIRECTORY = "downloads"
        }
    }
