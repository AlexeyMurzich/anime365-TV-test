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

/** Что делает касание Перевода: смотреть или скачать эту серию, или эту и следующие той же Командой. */
enum class PickerPurpose { Watch, DownloadEpisode, DownloadFollowing }

/** Выбор Перевода — нижний лист поверх экрана Аниме или Плеера (docs/screens.md). */
@Serializable
data class TranslationsKey(
    val seriesId: Int,
    val episodeId: Int,
    val purpose: PickerPurpose = PickerPurpose.Watch,
) : NavKey

/**
 * [onOpenEpisode] — Выбор Перевода для Эпизода, [onPlay] — Плеер сразу, [onDownloadEpisode] — Выбор
 * Перевода для Скачивания. [onLogin] и [onSubscription] ведут туда, где без входа и Подписки смотреть нельзя.
 */
fun EntryProviderScope<NavKey>.seriesEntries(
    onOpenGenre: (genreId: Int) -> Unit,
    onOpenEpisode: (seriesId: Int, episodeId: Int) -> Unit,
    onDownloadEpisode: (seriesId: Int, episodeId: Int, following: Boolean) -> Unit,
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
            onDownloadEpisode = { episodeId, following -> onDownloadEpisode(key.id, episodeId, following) },
            onPlay = { episodeId, translationId -> onPlay(key.id, episodeId, translationId) },
            onBack = onBack,
        )
    }
    entry<TranslationsKey>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
        TranslationPickerRoute(
            seriesId = key.seriesId,
            episodeId = key.episodeId,
            purpose = key.purpose,
            onPlay = { onPlay(key.seriesId, key.episodeId, it) },
            onLogin = onLogin,
            onSubscription = onSubscription,
            onClose = onBack,
        )
    }
}
