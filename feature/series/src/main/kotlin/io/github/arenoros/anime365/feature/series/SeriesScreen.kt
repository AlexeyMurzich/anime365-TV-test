package io.github.arenoros.anime365.feature.series

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.arenoros.anime365.core.data.repository.SeriesUnavailableException
import io.github.arenoros.anime365.core.designsystem.component.EmptyState
import io.github.arenoros.anime365.core.designsystem.component.EpisodeButton
import io.github.arenoros.anime365.core.designsystem.component.ErrorState
import io.github.arenoros.anime365.core.designsystem.component.KadrTopAppBar
import io.github.arenoros.anime365.core.designsystem.component.PosterStatus
import io.github.arenoros.anime365.core.designsystem.component.RatingCompact
import io.github.arenoros.anime365.core.designsystem.component.RatingLarge
import io.github.arenoros.anime365.core.designsystem.component.SkeletonLine
import io.github.arenoros.anime365.core.designsystem.component.StaleDataBanner
import io.github.arenoros.anime365.core.designsystem.component.StatusBadge
import io.github.arenoros.anime365.core.designsystem.component.errorMessage
import io.github.arenoros.anime365.core.designsystem.component.skeleton
import io.github.arenoros.anime365.core.designsystem.icon.KadrIcons
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.theme.episodeColumns
import io.github.arenoros.anime365.core.designsystem.tokens.KadrLayout
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.ReleaseStatus
import io.github.arenoros.anime365.core.model.Series
import io.github.arenoros.anime365.core.model.SeriesDetails
import io.github.arenoros.anime365.core.model.SeriesSummary

private val POSTER_WIDTH = 120.dp

private enum class SeriesTab { Description, Episodes }

@Composable
internal fun SeriesRoute(
    seriesId: Int,
    onOpenGenre: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: SeriesViewModel =
        hiltViewModel<SeriesViewModel, SeriesViewModel.Factory>(creationCallback = { it.create(seriesId) }),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SeriesScreen(state = state, onRetry = viewModel::refresh, onOpenGenre = onOpenGenre, onBack = onBack)
}

@Composable
internal fun SeriesScreen(
    state: SeriesUiState,
    onRetry: () -> Unit,
    onOpenGenre: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val series = state.series
    Scaffold(
        modifier = modifier,
        topBar = {
            KadrTopAppBar(
                title = series?.summary?.title.orEmpty(),
                onBack = onBack,
                actions = { series?.details?.siteUrl?.let { SiteMenu(it) } },
            )
        },
        containerColor = KadrTheme.palette.background,
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                series == null && state.error is SeriesUnavailableException -> {
                    EmptyState(stringResource(R.string.feature_series_unavailable))
                }

                series == null && state.error != null -> {
                    ErrorState(errorMessage(state.error), onRetry = onRetry)
                }

                series == null -> {
                    HeaderPlaceholder()
                }

                else -> {
                    SeriesContent(series, state, onRetry, onOpenGenre)
                }
            }
        }
    }
}

@Composable
private fun SeriesContent(
    series: Series,
    state: SeriesUiState,
    onRetry: () -> Unit,
    onOpenGenre: (Int) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(SeriesTab.Description) }
    val details = series.details
    BoxWithConstraints {
        val columns = episodeColumns(maxWidth)
        LazyColumn(contentPadding = PaddingValues(bottom = KadrSpacing.Space6)) {
            if (state.error != null && details != null) item { StaleDataBanner(state.error) }
            item { Header(series.summary, details) }
            if (details != null && details.genres.isNotEmpty()) item { Genres(details, onOpenGenre) }
            item {
                PrimaryTabRow(selectedTabIndex = tab.ordinal, containerColor = KadrTheme.palette.surface) {
                    SeriesTab.entries.forEach { entry ->
                        Tab(
                            selected = tab == entry,
                            onClick = { tab = entry },
                            text = {
                                Text(
                                    stringResource(
                                        if (entry == SeriesTab.Description) {
                                            R.string.feature_series_tab_description
                                        } else {
                                            R.string.feature_series_tab_episodes
                                        },
                                    ).uppercase(),
                                )
                            },
                        )
                    }
                }
            }
            when {
                details == null && state.error != null -> {
                    item { ErrorState(errorMessage(state.error), onRetry = onRetry) }
                }

                details == null -> {
                    item { TextPlaceholder() }
                }

                tab == SeriesTab.Description -> {
                    description(details)
                }

                else -> {
                    episodes(details, series.summary, columns)
                }
            }
        }
    }
}

@Composable
private fun Header(
    summary: SeriesSummary,
    details: SeriesDetails?,
) {
    Row(
        Modifier.fillMaxWidth().background(KadrTheme.palette.surface).padding(KadrSpacing.Space4),
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space4),
    ) {
        AsyncImage(
            model = details?.largePosterUrl ?: summary.posterUrl,
            contentDescription = null,
            modifier =
                Modifier
                    .width(POSTER_WIDTH)
                    .aspectRatio(KadrLayout.PosterRatio)
                    .clip(MaterialTheme.shapes.small)
                    .background(KadrTheme.palette.surfaceVariant),
            contentScale = ContentScale.Crop,
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KadrSpacing.Space2)) {
            Text(
                summary.title,
                Modifier.semantics { heading() },
                // Длинные русские названия при крупном шрифте иначе рвутся посреди слова.
                style = MaterialTheme.typography.headlineSmall.copy(hyphens = Hyphens.Auto),
                maxLines = 3,
            )
            summary.originalTitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = KadrTheme.palette.inkMuted)
            }
            val episodes = summary.episodeCount?.let { stringResource(R.string.feature_series_episodes_count, it) }
            Text(
                listOfNotNull(summary.kind, episodes, summary.season ?: summary.year?.toString()).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = KadrTheme.palette.inkMuted,
            )
            StatusBadge(summary.status.toPosterStatus())
            val score = summary.score
            if (score != null) RatingLarge(score) else RatingCompact(null)
        }
    }
}

@Composable
private fun Genres(
    details: SeriesDetails,
    onOpenGenre: (Int) -> Unit,
) {
    FlowRow(
        Modifier.fillMaxWidth().background(KadrTheme.palette.surface).padding(horizontal = KadrSpacing.Space4),
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
    ) {
        details.genres.forEach { genre ->
            AssistChip(onClick = { onOpenGenre(genre.id) }, label = { Text(genre.title) })
        }
    }
}

private fun LazyListScope.description(details: SeriesDetails) {
    item {
        var source by rememberSaveable { mutableIntStateOf(0) }
        Column(Modifier.padding(KadrSpacing.Space4), verticalArrangement = Arrangement.spacedBy(KadrSpacing.Space3)) {
            if (details.descriptions.size > 1) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2)) {
                    details.descriptions.forEachIndexed { index, description ->
                        FilterChip(
                            selected = index == source,
                            onClick = { source = index },
                            label = { Text(description.source) },
                        )
                    }
                }
            }
            val text = details.descriptions.getOrNull(source)?.text
            Text(
                text ?: stringResource(R.string.feature_series_no_description),
                style = MaterialTheme.typography.bodyLarge,
                color = if (text == null) KadrTheme.palette.inkMuted else KadrTheme.palette.ink,
            )
        }
    }
    if (details.links.isEmpty()) return
    item {
        Text(
            stringResource(R.string.feature_series_links),
            Modifier.padding(horizontal = KadrSpacing.Space4).semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
        )
    }
    item {
        val uriHandler = LocalUriHandler.current
        FlowRow(Modifier.padding(horizontal = KadrSpacing.Space2)) {
            details.links.forEach { link ->
                TextButton(onClick = { uriHandler.openUri(link.url) }) { Text(link.title) }
            }
        }
    }
}

private fun LazyListScope.episodes(
    details: SeriesDetails,
    summary: SeriesSummary,
    columns: Int,
) {
    val grid = details.episodeGrid(summary.episodeCount)
    if (grid.special.isEmpty() && grid.numbered.isEmpty()) {
        item { EmptyState(stringResource(R.string.feature_series_no_episodes)) }
        return
    }
    // M1: касание Эпизода ничего не делает — Выбор Перевода и Плеер появятся в M2.
    items(grid.special.chunked((columns / 2).coerceAtLeast(1))) { row ->
        GridRow {
            row.forEach { episode ->
                EpisodeButton(
                    label = episode.label,
                    description = episode.label,
                    released = true,
                    onClick = null,
                    modifier = Modifier.weight(2f),
                    wide = true,
                )
            }
            repeat(columns / 2 - row.size) { Spacer(Modifier.weight(2f)) }
        }
    }
    item {
        var range by rememberSaveable {
            mutableIntStateOf(
                if (summary.status ==
                    ReleaseStatus.Ongoing
                ) {
                    grid.ranges.lastIndex
                } else {
                    0
                },
            )
        }
        Column {
            if (grid.ranges.isNotEmpty()) RangeChips(grid, range, onSelect = { range = it })
            val cells = grid.ranges.getOrNull(range)?.let { grid.numbered.slice(it) } ?: grid.numbered
            cells.chunked(columns).forEach { row ->
                GridRow {
                    row.forEach { cell -> NumberedEpisodeButton(cell, Modifier.weight(1f)) }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun RangeChips(
    grid: EpisodeGrid,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    FlowRow(
        Modifier.padding(horizontal = KadrSpacing.Space4),
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
    ) {
        grid.ranges.forEachIndexed { index, range ->
            val label =
                stringResource(
                    R.string.feature_series_episode_range,
                    grid.numbered[range.first].number,
                    grid.numbered[range.last].number,
                )
            FilterChip(selected = index == selected, onClick = { onSelect(index) }, label = { Text(label) })
        }
    }
}

@Composable
private fun NumberedEpisodeButton(
    cell: EpisodeCell,
    modifier: Modifier,
) {
    when (cell) {
        is EpisodeCell.Released -> {
            EpisodeButton(cell.number, cell.episode.label, released = true, onClick = null, modifier = modifier)
        }

        is EpisodeCell.Upcoming -> {
            EpisodeButton(
                cell.number,
                stringResource(R.string.feature_series_episode_upcoming, cell.number),
                released = false,
                onClick = null,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun GridRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = KadrSpacing.Space4, vertical = KadrSpacing.Space1),
        horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
        content = content,
    )
}

@Composable
private fun SiteMenu(siteUrl: String) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val shareTitle = stringResource(R.string.feature_series_share)
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(painterResource(KadrIcons.MoreVert), stringResource(R.string.feature_series_more))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.feature_series_open_on_site)) },
                onClick = {
                    expanded = false
                    uriHandler.openUri(siteUrl)
                },
            )
            DropdownMenuItem(
                text = { Text(shareTitle) },
                onClick = {
                    expanded = false
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, siteUrl)
                    context.startActivity(Intent.createChooser(send, shareTitle))
                },
            )
        }
    }
}

@Composable
private fun HeaderPlaceholder() {
    Row(Modifier.padding(KadrSpacing.Space4), horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space4)) {
        Box(Modifier.width(POSTER_WIDTH).aspectRatio(KadrLayout.PosterRatio).skeleton())
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KadrSpacing.Space2)) {
            SkeletonLine(Modifier.fillMaxWidth(), height = 24.dp)
            SkeletonLine(Modifier.fillMaxWidth(fraction = 0.6f))
            SkeletonLine(Modifier.fillMaxWidth(fraction = 0.8f))
        }
    }
}

@Composable
private fun TextPlaceholder() {
    Column(Modifier.padding(KadrSpacing.Space4), verticalArrangement = Arrangement.spacedBy(KadrSpacing.Space2)) {
        repeat(PLACEHOLDER_LINES) { SkeletonLine(Modifier.fillMaxWidth()) }
        SkeletonLine(Modifier.fillMaxWidth(fraction = 0.5f))
        Spacer(Modifier.height(KadrSpacing.Space2))
    }
}

private const val PLACEHOLDER_LINES = 4

private fun ReleaseStatus.toPosterStatus(): PosterStatus =
    when (this) {
        ReleaseStatus.Ongoing -> PosterStatus.Ongoing
        ReleaseStatus.Released -> PosterStatus.Released
        ReleaseStatus.Announced -> PosterStatus.Announced
    }
