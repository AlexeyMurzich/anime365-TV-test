package io.github.arenoros.anime365

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp

// Срок кэша постеров — docs/roadmap.md, M1.
private const val POSTER_CACHE_BYTES = 200L * 1024 * 1024

@HiltAndroidApp
class Anime365Application :
    Application(),
    SingletonImageLoader.Factory {
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
