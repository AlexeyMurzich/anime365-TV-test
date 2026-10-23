package io.github.arenoros.anime365.feature.series.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.arenoros.anime365.feature.series.SeriesRoute
import kotlinx.serialization.Serializable

@Serializable
data class SeriesKey(
    val id: Int,
) : NavKey

fun EntryProviderScope<NavKey>.seriesEntries(
    onOpenGenre: (genreId: Int) -> Unit,
    onBack: () -> Unit,
) {
    entry<SeriesKey> { key -> SeriesRoute(seriesId = key.id, onOpenGenre = onOpenGenre, onBack = onBack) }
}
