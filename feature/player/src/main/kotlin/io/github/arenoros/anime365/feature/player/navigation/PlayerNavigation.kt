package io.github.arenoros.anime365.feature.player.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.arenoros.anime365.feature.player.PlayerRoute
import kotlinx.serialization.Serializable

/**
 * Плеер одного Перевода. Живёт в своём стеке поверх табов (ADR 0011); развёрнутый скрывает табы и
 * системные панели (docs/screens.md).
 */
@Serializable
data class PlayerKey(
    val seriesId: Int,
    val episodeId: Int,
    val translationId: Int,
) : NavKey

/**
 * [onChooseTranslation] — Выбор Перевода для Эпизода этого же аниме поверх Плеера.
 * [onMinimize] — свернуть в окно поверх приложения ([io.github.arenoros.anime365.feature.player.MiniPlayer]).
 */
fun EntryProviderScope<NavKey>.playerEntries(
    onChooseTranslation: (seriesId: Int, episodeId: Int) -> Unit,
    onLogin: () -> Unit,
    onSubscription: () -> Unit,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
) {
    entry<PlayerKey> { key ->
        PlayerRoute(
            key = key,
            onChooseTranslation = { onChooseTranslation(key.seriesId, it) },
            onLogin = onLogin,
            onSubscription = onSubscription,
            onMinimize = onMinimize,
            onClose = onClose,
        )
    }
}
