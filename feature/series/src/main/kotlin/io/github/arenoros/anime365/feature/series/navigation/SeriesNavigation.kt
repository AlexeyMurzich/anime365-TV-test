package io.github.arenoros.anime365.feature.series.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.arenoros.anime365.core.designsystem.component.BottomSheetSceneStrategy
import io.github.arenoros.anime365.feature.series.SeriesRoute
import io.github.arenoros.anime365.feature.series.translations.TranslationPickerRoute
import kotlinx.serialization.Serializable

@Serializable
data class SeriesKey(
    val id: Int,
) : NavKey

/** Выбор Перевода — нижний лист поверх экрана Аниме или Плеера (docs/screens.md). */
@Serializable
data class TranslationsKey(
    val seriesId: Int,
    val episodeId: Int,
) : NavKey

/**
 * [onOpenEpisode] — Выбор Перевода для Эпизода, [onPlay] — Плеер сразу. [onLogin] и [onSubscription]
 * ведут туда, где без входа и Подписки смотреть нельзя.
 */
fun EntryProviderScope<NavKey>.seriesEntries(
    onOpenGenre: (genreId: Int) -> Unit,
    onOpenEpisode: (seriesId: Int, episodeId: Int) -> Unit,
    onPlay: (seriesId: Int, episodeId: Int, translationId: Int) -> Unit,
    onLogin: () -> Unit,
    onSubscription: () -> Unit,
    onBack: () -> Unit,
) {
    entry<SeriesKey> { key ->
        SeriesRoute(
            seriesId = key.id,
            onOpenGenre = onOpenGenre,
            onOpenEpisode = { onOpenEpisode(key.id, it) },
            onPlay = { episodeId, translationId -> onPlay(key.id, episodeId, translationId) },
            onBack = onBack,
        )
    }
    entry<TranslationsKey>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        TranslationPickerRoute(
            seriesId = key.seriesId,
            episodeId = key.episodeId,
            onPlay = { onPlay(key.seriesId, key.episodeId, it) },
            onLogin = onLogin,
            onSubscription = onSubscription,
        )
    }
}
