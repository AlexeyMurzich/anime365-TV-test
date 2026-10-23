package io.github.arenoros.anime365.feature.library.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.arenoros.anime365.feature.library.LibraryRoute
import kotlinx.serialization.Serializable

@Serializable
data object LibraryKey : NavKey

fun EntryProviderScope<NavKey>.libraryEntries(
    onOpenSeries: (Int) -> Unit,
    onOpenEpisode: (seriesId: Int, episodeId: Int) -> Unit,
    onPlay: (seriesId: Int, episodeId: Int, translationId: Int) -> Unit,
) {
    entry<LibraryKey> { LibraryRoute(onOpenSeries, onOpenEpisode, onPlay) }
}
