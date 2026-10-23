package io.github.arenoros.anime365.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import io.github.arenoros.anime365.core.designsystem.component.BottomSheetSceneStrategy
import io.github.arenoros.anime365.core.designsystem.component.KadrNavigationBar
import io.github.arenoros.anime365.core.designsystem.component.KadrNavigationBarItem
import io.github.arenoros.anime365.feature.account.navigation.LoginKey
import io.github.arenoros.anime365.feature.account.navigation.SubscriptionKey
import io.github.arenoros.anime365.feature.account.navigation.accountEntries
import io.github.arenoros.anime365.feature.catalog.navigation.CatalogKey
import io.github.arenoros.anime365.feature.catalog.navigation.catalogEntries
import io.github.arenoros.anime365.feature.library.navigation.libraryEntries
import io.github.arenoros.anime365.feature.player.navigation.PlayerKey
import io.github.arenoros.anime365.feature.player.navigation.playerEntries
import io.github.arenoros.anime365.feature.series.navigation.SeriesKey
import io.github.arenoros.anime365.feature.series.navigation.TranslationsKey
import io.github.arenoros.anime365.feature.series.navigation.seriesEntries
import io.github.arenoros.anime365.navigation.TopLevelDestination

@Composable
fun Anime365App(modifier: Modifier = Modifier) {
    val backStacks = TopLevelDestination.entries.associateWith { rememberNavBackStack(it.key) }
    var current by rememberSaveable { mutableStateOf(TopLevelDestination.Home) }
    val backStack = backStacks.getValue(current)

    val decorators =
        listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator(),
        )
    // Записи всех табов живут всё время: так переключение таба не сбрасывает ни прокрутку, ни ViewModel.
    // Ключ записи — с именем таба: одно и то же Аниме, открытое в двух табах, — два разных экрана.
    val entries =
        TopLevelDestination.entries.associateWith { destination ->
            val tabStack = backStacks.getValue(destination)
            val provider = remember(tabStack) { entryProvider(tabStack) }
            rememberDecoratedNavEntries(tabStack, decorators) { key ->
                val entry = provider(key)
                NavEntry(key, "${destination.name}/${entry.contentKey}", entry.metadata) { entry.Content() }
            }
        }
    val sceneStrategies = remember { listOf(BottomSheetSceneStrategy<NavKey>(), SinglePaneSceneStrategy()) }

    // «Назад» с корня любого таба, кроме Главной, ведёт на Главную, а не закрывает приложение.
    BackHandler(enabled = current != TopLevelDestination.Home && backStack.size == 1) {
        current = TopLevelDestination.Home
    }

    // Плеер скрывает табы (docs/screens.md); лист Выбора Перевода поверх него — тоже без табов.
    val playerOnTop = backStack.lastOrNull { it !is TranslationsKey } is PlayerKey
    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (!playerOnTop) {
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
            }
        },
        // Верхний отступ у каждого экрана свой: зелёная шапка заходит под статус-бар.
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        NavDisplay(
            entries = entries.getValue(current),
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            sceneStrategies = sceneStrategies,
            onBack = { backStack.removeLastOrNull() },
        )
    }
}

/** Экраны всех фич. Переходы кладут ключ в стек того таба, где открыт экран (ADR 0009). */
private fun entryProvider(backStack: MutableList<NavKey>): (NavKey) -> NavEntry<NavKey> {
    val openEpisode: (Int, Int) -> Unit = { seriesId, episodeId -> backStack.add(TranslationsKey(seriesId, episodeId)) }
    // Плеер открывается вместо листа Выбора Перевода и вместо прежнего Плеера: «Назад» ведёт туда,
    // откуда начали смотреть, а не к прошлому Переводу.
    val play: (Int, Int, Int) -> Unit = { seriesId, episodeId, translationId ->
        if (backStack.lastOrNull() is TranslationsKey) backStack.removeLastOrNull()
        if (backStack.lastOrNull() is PlayerKey) backStack.removeLastOrNull()
        backStack.add(PlayerKey(seriesId, episodeId, translationId))
    }
    val login: () -> Unit = { backStack.add(LoginKey) }
    val subscription: () -> Unit = { backStack.add(SubscriptionKey) }
    val back: () -> Unit = { backStack.removeLastOrNull() }
    return entryProvider {
        catalogEntries(
            onOpenSeries = { backStack.add(SeriesKey(it)) },
            onOpenEpisode = openEpisode,
            onPlay = play,
            onOpenCatalog = { backStack.add(it) },
            onBack = back,
        )
        seriesEntries(
            onOpenGenre = { backStack.add(CatalogKey(genreId = it)) },
            onOpenEpisode = openEpisode,
            onPlay = play,
            onLogin = login,
            onSubscription = subscription,
            onBack = back,
        )
        playerEntries(
            onChooseTranslation = openEpisode,
            onLogin = login,
            onSubscription = subscription,
            onBack = back,
        )
        libraryEntries()
        accountEntries(onLogin = login, onBack = back)
    }
}
