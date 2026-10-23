package io.github.arenoros.anime365.feature.catalog.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.arenoros.anime365.feature.catalog.catalog.CatalogRoute
import io.github.arenoros.anime365.feature.catalog.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : NavKey

/**
 * Каталог. Корень таба — [CatalogKey.Root]; с параметрами Каталог открывается поверх другого
 * экрана: «ВСЕ» у Онгоингов, жанр на экране Аниме, поиск с Главной.
 */
@Serializable
data class CatalogKey(
    val genreId: Int? = null,
    val ongoingOnly: Boolean = false,
    val focusSearch: Boolean = false,
) : NavKey {
    companion object {
        val Root = CatalogKey()
    }
}

/** [onOpenEpisode] — Выбор Перевода для Эпизода, [onPlay] — Плеер сразу. */
fun EntryProviderScope<NavKey>.catalogEntries(
    onOpenSeries: (seriesId: Int) -> Unit,
    onOpenEpisode: (seriesId: Int, episodeId: Int) -> Unit,
    onPlay: (seriesId: Int, episodeId: Int, translationId: Int) -> Unit,
    onOpenCatalog: (CatalogKey) -> Unit,
    onBack: () -> Unit,
) {
    entry<HomeKey> {
        HomeRoute(
            onOpenSeries = onOpenSeries,
            onOpenEpisode = onOpenEpisode,
            onPlay = onPlay,
            onOpenSearch = { onOpenCatalog(CatalogKey(focusSearch = true)) },
            onShowAllOngoing = { onOpenCatalog(CatalogKey(ongoingOnly = true)) },
        )
    }
    entry<CatalogKey> { key ->
        CatalogRoute(
            key = key,
            onOpenSeries = onOpenSeries,
            onBack = if (key == CatalogKey.Root) null else onBack,
        )
    }
}
