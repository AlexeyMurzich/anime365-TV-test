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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrRadius
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationKind
import io.github.arenoros.anime365.feature.series.R

private const val PLACEHOLDER_ROWS = 4

@Composable
internal fun TranslationPickerRoute(
    seriesId: Int,
    episodeId: Int,
    onPlay: (translationId: Int) -> Unit,
    onLogin: () -> Unit,
    onSubscription: () -> Unit,
    viewModel: TranslationPickerViewModel =
        hiltViewModel<TranslationPickerViewModel, TranslationPickerViewModel.Factory>(
            key = "translations/$episodeId",
            creationCallback = { it.create(seriesId, episodeId) },
        ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TranslationPickerSheet(
        state = state,
        onSelectKind = viewModel::selectKind,
        onRetry = viewModel::refresh,
        onSelect = { translation ->
            when (state.access) {
                PlaybackAccess.Allowed -> onPlay(translation.id)
                PlaybackAccess.NeedsLogin -> onLogin()
                PlaybackAccess.NeedsSubscription -> onSubscription()
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
    Column(modifier.fillMaxWidth()) {
        Text(
            state.episodeLabel ?: stringResource(R.string.feature_series_translations_title),
            Modifier.padding(horizontal = KadrSpacing.Space4).semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
        AccessBanner(state.access)
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
                LazyColumn(contentPadding = PaddingValues(bottom = KadrSpacing.Space6)) {
                    items(state.shown, key = { it.id }) { translation ->
                        TranslationRow(
                            translation,
                            state.isRemembered(translation),
                            onClick = { onSelect(translation) },
                        )
                    }
                }
            }
        }
    }
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
                selected = kind == selected,
                onClick = { onSelect(kind) },
                label = { Text(stringResource(kind.label())) },
            )
        }
    }
}

/** Команда, Источник и Разрешение. Вшитые или Отдельные субтитры — становится известно при запуске. */
@Composable
private fun TranslationRow(
    translation: Translation,
    remembered: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = KadrLayout.TouchTarget + KadrSpacing.Space2)
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
