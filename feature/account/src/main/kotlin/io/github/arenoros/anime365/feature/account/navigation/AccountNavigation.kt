package io.github.arenoros.anime365.feature.account.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.arenoros.anime365.feature.account.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
data object ProfileKey : NavKey

fun EntryProviderScope<NavKey>.accountEntries() {
    entry<ProfileKey> { ProfileScreen() }
}
