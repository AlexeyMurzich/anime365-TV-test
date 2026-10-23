package io.github.arenoros.anime365.feature.catalog.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.arenoros.anime365.feature.catalog.CatalogScreen
import io.github.arenoros.anime365.feature.catalog.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : NavKey

@Serializable
data object CatalogKey : NavKey

fun EntryProviderScope<NavKey>.catalogEntries() {
    entry<HomeKey> { HomeScreen() }
    entry<CatalogKey> { CatalogScreen() }
}
