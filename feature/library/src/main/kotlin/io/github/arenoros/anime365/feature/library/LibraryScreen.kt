package io.github.arenoros.anime365.feature.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arenoros.anime365.core.designsystem.component.EmptyState
import io.github.arenoros.anime365.core.designsystem.component.KadrTopAppBar
import io.github.arenoros.anime365.core.designsystem.component.LibraryListItem
import io.github.arenoros.anime365.core.designsystem.component.ListItemPlaceholder
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.EpisodeDownload
import io.github.arenoros.anime365.core.model.LibraryEntry
import io.github.arenoros.anime365.core.model.LibraryStatus
import io.github.arenoros.anime365.core.ui.label

private const val PLACEHOLDER_ROWS = 6

@Composable
internal fun LibraryRoute(
    onOpenSeries: (Int) -> Unit,
    onOpenEpisode: (seriesId: Int, episodeId: Int) -> Unit,
    onPlay: (seriesId: Int, episodeId: Int, translationId: Int) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryEvent.Play -> onPlay(event.seriesId, event.episodeId, event.translationId)
                is LibraryEvent.ChooseTranslation -> onOpenEpisode(event.seriesId, event.episodeId)
                is LibraryEvent.OpenSeries -> onOpenSeries(event.seriesId)
            }
        }
    }
    LibraryScreen(
        state = state,
        onOpenSeries = onOpenSeries,
        onContinue = viewModel::continueWatching,
        onDismissHint = viewModel::dismissHint,
        downloadActions =
            DownloadActions(
                onPlay = { onPlay(it.seriesId, it.episode.id, it.translation.id) },
                onRetry = viewModel::retryDownload,
                onRemove = viewModel::removeDownloads,
            ),
    )
}

internal class DownloadActions(
    val onPlay: (EpisodeDownload) -> Unit,
    val onRetry: (EpisodeDownload) -> Unit,
    val onRemove: (List<EpisodeDownload>) -> Unit,
)

/** Список: вкладки по Статусам и последняя — Скачанное. */
@Composable
internal fun LibraryScreen(
    state: LibraryUiState,
    onOpenSeries: (Int) -> Unit,
    onContinue: (Int) -> Unit,
    onDismissHint: () -> Unit,
    modifier: Modifier = Modifier,
    downloadActions: DownloadActions = DownloadActions({}, {}, {}),
    initialTab: Int = 0,
) {
    // Вкладки Статусов по порядку, за ними — Скачанное.
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    val status = LibraryStatus.entries.getOrNull(tab)
    Scaffold(
        modifier = modifier,
        topBar = { KadrTopAppBar(stringResource(R.string.feature_library_title)) },
        containerColor = KadrTheme.palette.background,
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            PrimaryScrollableTabRow(
                selectedTabIndex = tab,
                containerColor = KadrTheme.palette.surface,
                edgePadding = KadrSpacing.Space2,
            ) {
                LibraryStatus.entries.forEach { entry ->
                    Tab(
                        selected = status == entry,
                        onClick = { tab = entry.ordinal },
                        text = { Text(entry.label().uppercase()) },
                    )
                }
                Tab(
                    selected = status == null,
                    onClick = { tab = LibraryStatus.entries.size },
                    text = { Text(stringResource(R.string.feature_library_downloads_tab).uppercase()) },
                )
            }
            if (!state.hintShown) LocalOnlyHint(onDismissHint)
            val entries = state.entries
            when {
                status == null -> {
                    DownloadsTab(
                        state.downloads,
                        onPlay = downloadActions.onPlay,
                        onRetry = downloadActions.onRetry,
                        onRemove = downloadActions.onRemove,
                    )
                }

                entries == null -> {
                    repeat(PLACEHOLDER_ROWS) { ListItemPlaceholder(thumbnailWidth = 56.dp) }
                }

                state.of(status).isEmpty() -> {
                    EmptyState(stringResource(R.string.feature_library_empty))
                }

                else -> {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(state.of(status), key = { it.seriesId }) { entry ->
                            LibraryRow(entry, onOpenSeries, onContinue)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(
    entry: LibraryEntry,
    onOpenSeries: (Int) -> Unit,
    onContinue: (Int) -> Unit,
) {
    val total = entry.episodeCount?.takeIf { it > 0 }
    val newEpisodes = entry.newEpisodes.takeIf { it > 0 && entry.status == LibraryStatus.Watching }
    LibraryListItem(
        imageUrl = entry.posterUrl,
        title = entry.title,
        subtitle =
            if (total != null) {
                stringResource(R.string.feature_library_watched_of, entry.watchedEpisodes, total)
            } else {
                stringResource(R.string.feature_library_watched, entry.watchedEpisodes)
            },
        onClick = { onOpenSeries(entry.seriesId) },
        actionIcon = KadrIcons.PlayArrow,
        actionLabel = stringResource(R.string.feature_library_continue, entry.title),
        onAction = { onContinue(entry.seriesId) },
        badge = newEpisodes?.let { "+$it" },
        badgeDescription = newEpisodes?.let { pluralStringResource(R.plurals.feature_library_new_episodes, it, it) },
    )
}

/** Один раз: Список не на сервере (ADR 0001), и где сохранить копию. */
@Composable
private fun LocalOnlyHint(onDismiss: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(KadrSpacing.Space4),
        colors = CardDefaults.cardColors(containerColor = KadrTheme.palette.surfaceVariant),
    ) {
        Column(Modifier.padding(start = KadrSpacing.Space4, top = KadrSpacing.Space4, end = KadrSpacing.Space2)) {
            Text(stringResource(R.string.feature_library_local_hint), style = MaterialTheme.typography.bodyMedium)
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.feature_library_hint_ok).uppercase()) }
            }
        }
    }
}
