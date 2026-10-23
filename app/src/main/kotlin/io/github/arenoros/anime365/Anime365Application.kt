package io.github.arenoros.anime365

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import io.github.arenoros.anime365.feature.library.notifications.NewEpisodesCheck
import javax.inject.Inject

// Срок кэша постеров — docs/roadmap.md, M1.
private const val POSTER_CACHE_BYTES = 200L * 1024 * 1024

@HiltAndroidApp
class Anime365Application :
    Application(),
    SingletonImageLoader.Factory,
    Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        NewEpisodesCheck.schedule(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader
            .Builder(context)
            .crossfade(true)
            .diskCache {
                DiskCache
                    .Builder()
                    .directory(context.cacheDir.resolve("posters"))
                    .maxSizeBytes(POSTER_CACHE_BYTES)
                    .build()
            }.build()
}
