package io.github.arenoros.anime365.feature.series.translations

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arenoros.anime365.core.designsystem.component.EmptyState
import io.github.arenoros.anime365.core.designsystem.component.ErrorState
import io.github.arenoros.anime365.core.designsystem.component.SkeletonLine
import io.github.arenoros.anime365.core.designsystem.component.StaleDataBanner
import io.github.arenoros.anime365.core.designsystem.component.errorMessage
import io.github.arenoros.anime365.core.designsystem.component.rememberTvInitialFocus
import io.github.arenoros.anime365.core.designsystem.component.tvFocusIndicator
import io.github.arenoros.anime365.core.designsystem.component.tvInitialFocus
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrRadius
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.DownloadState
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationKind
import io.github.arenoros.anime365.feature.series.R
import io.github.arenoros.anime365.feature.series.navigation.PickerPurpose

private const val PLACEHOLDER_ROWS = 4

@Composable
internal fun TranslationPickerRoute(
    seriesId: Int,
    episodeId: Int,
    purpose: PickerPurpose,
    onPlay: (translationId: Int) -> Unit,
    onLogin: () -> Unit,
    onSubscription: () -> Unit,
    onClose: () -> Unit,
    viewModel: TranslationPickerViewModel =
        hiltViewModel<TranslationPickerViewModel, TranslationPickerViewModel.Factory>(
            key = "translations/$episodeId/$purpose",
            creationCallback = { it.create(seriesId, episodeId, purpose) },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PickerEvent.Enqueued -> onClose()
                PickerEvent.NeedsLogin -> onLogin()
                PickerEvent.NeedsSubscription -> onSubscription()
            }
        }
    }
    TranslationPickerSheet(
        state = state,
        onSelectKind = viewModel::selectKind,
        onRetry = viewModel::refresh,
        onSelect = { translation ->
            when (state.access) {
                PlaybackAccess.Allowed -> {
                    if (purpose == PickerPurpose.Watch) onPlay(translation.id) else viewModel.download(translation)
                }

                PlaybackAccess.NeedsLogin -> {
                    onLogin()
                }

                PlaybackAccess.NeedsSubscription -> {
                    onSubscription()
                }
            }
        },
    )
}

/** TranslationSelector Kadr: шаг 1 — Вид перевода, шаг 2 — Переводы этого Вида в порядке сайта. */
@Composable
internal fun TranslationPickerSheet(
    state: TranslationPickerUiState,
    onSelectKind: (TranslationKind) -> Unit,
    onRetry: () -> Unit,
    onSelect: (Translation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialFocus = rememberTvInitialFocus()
    Column(modifier.fillMaxWidth()) {
        Text(
            state.episodeLabel ?: stringResource(R.string.feature_series_translations_title),
            Modifier.padding(horizontal = KadrSpacing.Space4).semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
        PurposeLine(state.purpose)
        if (state.isEnqueuing) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = KadrSpacing.Space2))
        AccessBanner(state.access)
        state.downloadError?.let { StaleDataBanner(errorMessage(it), Modifier.padding(top = KadrSpacing.Space2)) }
        val error = state.error
        when {
            state.translations.isEmpty() && state.isRefreshing -> {
                repeat(PLACEHOLDER_ROWS) { RowPlaceholder() }
            }

            state.translations.isEmpty() && error != null -> {
                ErrorState(errorMessage(error), onRetry = onRetry)
            }

            state.translations.isEmpty() -> {
                EmptyState(stringResource(R.string.feature_series_translations_empty))
            }

            else -> {
                if (error != null) StaleDataBanner(error)
                KindChips(state.kinds, state.selectedKind, onSelectKind)
                val firstId = state.shown.firstOrNull()?.id
                LazyColumn(contentPadding = PaddingValues(bottom = KadrSpacing.Space6)) {
                    items(state.shown, key = { it.id }) { translation ->
                        val focus = if (translation.id == firstId) Modifier.tvInitialFocus(initialFocus) else Modifier
                        TranslationRow(
                            translation,
                            state.isRemembered(translation),
                            state.downloads[translation.id],
                            onClick = { onSelect(translation) },
                            modifier = focus,
                        )
                    }
                }
            }
        }
    }
}

/** В режиме Скачивания — что именно поставится в очередь. */
@Composable
private fun PurposeLine(purpose: PickerPurpose) {
    val text =
        when (purpose) {
            PickerPurpose.Watch -> return
            PickerPurpose.DownloadEpisode -> R.string.feature_series_translations_download_episode
            PickerPurpose.DownloadFollowing -> R.string.feature_series_translations_download_following
        }
    Text(
        stringResource(text),
        Modifier.padding(horizontal = KadrSpacing.Space4),
        style = MaterialTheme.typography.bodyMedium,
        color = KadrTheme.palette.inkMuted,
    )
}

@Composable
private fun AccessBanner(access: PlaybackAccess) {
    val text =
        when (access) {
            PlaybackAccess.Allowed -> return
            PlaybackAccess.NeedsLogin -> R.string.feature_series_translations_needs_login
            PlaybackAccess.NeedsSubscription -> R.string.feature_series_translations_needs_subscription
        }
    StaleDataBanner(stringResource(text), Modifier.padding(top = KadrSpacing.Space2))
}

@Composable
private fun KindChips(
    kinds: List<TranslationKind>,
    selected: TranslationKind?,
    onSelect: (TranslationKind) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = KadrSpacing.Space4, vertical = KadrSpacing.Space2),
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
    ) {
        items(kinds) { kind ->
            FilterChip(
                modifier = Modifier.tvFocusIndicator(),
                selected = kind == selected,
                onClick = { onSelect(kind) },
                label = { Text(stringResource(kind.label())) },
            )
        }
    }
}

/**
 * Команда, Источник и Разрешение. Вшитые или Отдельные субтитры — становится известно при запуске.
 * Скачанный Перевод играет без сети — это видно в строке.
 */
@Composable
private fun TranslationRow(
    translation: Translation,
    remembered: Boolean,
    download: DownloadState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = KadrLayout.TouchTarget + KadrSpacing.Space2)
            .tvFocusIndicator()
            .clickable(onClick = onClick)
            .padding(horizontal = KadrSpacing.Space4, vertical = KadrSpacing.Space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                translation.team.ifEmpty { stringResource(R.string.feature_series_translations_no_team) },
                style = MaterialTheme.typography.bodyLarge,
                color = if (translation.team.isEmpty()) KadrTheme.palette.inkMuted else KadrTheme.palette.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (remembered) {
                Text(
                    stringResource(R.string.feature_series_translations_remembered),
                    style = MaterialTheme.typography.bodySmall,
                    color = KadrTheme.palette.primary,
                )
            }
            if (download != null) {
                Text(
                    stringResource(
                        if (download == DownloadState.Completed) {
                            R.string.feature_series_translations_downloaded
                        } else {
                            R.string.feature_series_translations_downloading
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = KadrTheme.palette.inkMuted,
                )
            }
        }
        if (remembered) {
            Icon(
                painterResource(KadrIcons.Check),
                contentDescription = null,
                Modifier.size(20.dp),
                tint = KadrTheme.palette.primary,
            )
        }
        translation.source?.let { Badge(it.uppercase()) }
        if (translation.height > 0) {
            Text(
                stringResource(R.string.feature_series_translations_height, translation.height),
                style = MaterialTheme.typography.labelLarge,
                color = KadrTheme.palette.inkMuted,
            )
        }
    }
}

/** Скелетон той же высоты, что [TranslationRow]. */
@Composable
private fun RowPlaceholder() {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = KadrLayout.TouchTarget + KadrSpacing.Space2)
            .padding(horizontal = KadrSpacing.Space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonLine(Modifier.fillMaxWidth(fraction = 0.6f))
    }
}

@Composable
private fun Badge(text: String) {
    Text(
        text,
        Modifier
            .background(KadrTheme.palette.surfaceVariant, RoundedCornerShape(KadrRadius.Xs))
            .padding(horizontal = KadrSpacing.Space1, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = KadrTheme.palette.inkMuted,
    )
}

@StringRes
internal fun TranslationKind.label(): Int =
    when (this) {
        TranslationKind.VoiceRu -> R.string.feature_series_kind_voice_ru
        TranslationKind.SubRu -> R.string.feature_series_kind_sub_ru
        TranslationKind.VoiceUk -> R.string.feature_series_kind_voice_uk
        TranslationKind.VoiceEn -> R.string.feature_series_kind_voice_en
        TranslationKind.SubEn -> R.string.feature_series_kind_sub_en
        TranslationKind.SubJa -> R.string.feature_series_kind_sub_ja
        TranslationKind.VoiceOther -> R.string.feature_series_kind_voice_other
        TranslationKind.Raw -> R.string.feature_series_kind_raw
        TranslationKind.Other -> R.string.feature_series_kind_other
    }
