package io.github.arenoros.anime365.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.arenoros.anime365.core.designsystem.component.KadrNavigationBar
import io.github.arenoros.anime365.core.designsystem.component.KadrNavigationBarItem
import io.github.arenoros.anime365.feature.account.navigation.accountEntries
import io.github.arenoros.anime365.feature.catalog.navigation.catalogEntries
import io.github.arenoros.anime365.feature.library.navigation.libraryEntries
import io.github.arenoros.anime365.navigation.TopLevelDestination

@Composable
fun Anime365App(modifier: Modifier = Modifier) {
    val backStacks = TopLevelDestination.entries.associateWith { rememberNavBackStack(it.key) }
    var current by rememberSaveable { mutableStateOf(TopLevelDestination.Home) }
    val backStack = backStacks.getValue(current)

    // «Назад» с корня любого таба, кроме Главной, ведёт на Главную, а не закрывает приложение.
    BackHandler(enabled = current != TopLevelDestination.Home && backStack.size == 1) {
        current = TopLevelDestination.Home
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            KadrNavigationBar {
                TopLevelDestination.entries.forEach { destination ->
                    val selected = destination == current
                    KadrNavigationBarItem(
                        selected = selected,
                        onClick = {
                            // Повторное нажатие на активный таб возвращает к его корню.
                            if (selected) {
                                while (backStack.size > 1) backStack.removeLastOrNull()
                            } else {
                                current = destination
                            }
                        },
                        icon = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        label = stringResource(destination.label),
                    )
                }
            }
        },
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(padding),
            entryProvider =
                entryProvider {
                    catalogEntries()
                    libraryEntries()
                    accountEntries()
                },
        )
    }
}
