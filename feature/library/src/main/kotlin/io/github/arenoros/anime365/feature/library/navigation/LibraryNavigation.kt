package io.github.arenoros.anime365.feature.library.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.arenoros.anime365.feature.library.LibraryScreen
import kotlinx.serialization.Serializable

@Serializable
data object LibraryKey : NavKey

fun EntryProviderScope<NavKey>.libraryEntries() {
    entry<LibraryKey> { LibraryScreen() }
}
