package io.github.arenoros.anime365.feature.catalog.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.arenoros.anime365.core.designsystem.component.EpisodeFeedRow
import io.github.arenoros.anime365.core.designsystem.component.ErrorState
import io.github.arenoros.anime365.core.designsystem.component.KadrSearchButton
import io.github.arenoros.anime365.core.designsystem.component.KadrTopAppBar
import io.github.arenoros.anime365.core.designsystem.component.ListItemPlaceholder
import io.github.arenoros.anime365.core.designsystem.component.PosterCard
import io.github.arenoros.anime365.core.designsystem.component.PosterCardPlaceholder
import io.github.arenoros.anime365.core.designsystem.component.RankingListItem
import io.github.arenoros.anime365.core.designsystem.component.SectionHeader
import io.github.arenoros.anime365.core.designsystem.component.StaleDataBanner
import io.github.arenoros.anime365.core.designsystem.component.errorMessage
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.ContinueWatching
import io.github.arenoros.anime365.core.model.NewEpisode
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.feature.catalog.R
import io.github.arenoros.anime365.feature.catalog.SeriesPosterCard
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val POSTER_ROW_WIDTH = 112.dp
private const val PLACEHOLDER_ROWS = 4
private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@Composable
internal fun HomeRoute(
    onOpenSeries: (Int) -> Unit,
    onOpenEpisode: (seriesId: Int, episodeId: Int) -> Unit,
    onPlay: (seriesId: Int, episodeId: Int, translationId: Int) -> Unit,
    onOpenSearch: () -> Unit,
    onShowAllOngoing: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.Play -> onPlay(event.seriesId, event.episodeId, event.translationId)
                is HomeEvent.ChooseTranslation -> onOpenEpisode(event.seriesId, event.episodeId)
            }
        }
    }
    HomeScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpenSeries = onOpenSeries,
        onOpenEpisode = viewModel::openEpisode,
        onPlay = onPlay,
        onOpenSearch = onOpenSearch,
        onShowAllOngoing = onShowAllOngoing,
    )
}

/**
 * Главная: «Продолжить просмотр» — сразу Плеер с тем же Переводом; Новые серии — Выбор Перевода с
 * отмеченной Командой прошлого Эпизода. [zone] и [today] — ради скриншот-тестов с фиксированной датой.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onOpenSeries: (Int) -> Unit,
    onOpenEpisode: (seriesId: Int, episodeId: Int) -> Unit,
    onPlay: (seriesId: Int, episodeId: Int, translationId: Int) -> Unit,
    onOpenSearch: () -> Unit,
    onShowAllOngoing: () -> Unit,
    modifier: Modifier = Modifier,
    zone: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zone),
) {
    Scaffold(
        modifier = modifier,
        topBar = { KadrTopAppBar(stringResource(R.string.feature_catalog_home_title)) },
        containerColor = KadrTheme.palette.background,
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing && !state.isEmpty,
            onRefresh = onRefresh,
            modifier = Modifier.padding(padding).fillMaxSize(),
        ) {
            if (state.isEmpty && state.error != null) {
                ErrorState(errorMessage(state.error), onRetry = onRefresh)
            } else {
                HomeContent(
                    state,
                    zone,
                    today,
                    HomeActions(onOpenSeries, onOpenEpisode, onPlay, onOpenSearch, onShowAllOngoing),
                )
            }
        }
    }
}

private class HomeActions(
    val onOpenSeries: (Int) -> Unit,
    val onOpenEpisode: (seriesId: Int, episodeId: Int) -> Unit,
    val onPlay: (seriesId: Int, episodeId: Int, translationId: Int) -> Unit,
    val onOpenSearch: () -> Unit,
    val onShowAllOngoing: () -> Unit,
)

@Composable
private fun HomeContent(
    state: HomeUiState,
    zone: ZoneId,
    today: LocalDate,
    actions: HomeActions,
) {
    val days = remember(state.newEpisodes, zone) { state.newEpisodes.groupByDay(zone) }
    LazyColumn(contentPadding = PaddingValues(bottom = KadrSpacing.Space6)) {
        item(key = "search") {
            KadrSearchButton(
                placeholder = stringResource(R.string.feature_catalog_search_placeholder),
                onClick = actions.onOpenSearch,
                modifier = Modifier.padding(KadrSpacing.Space4),
            )
        }
        if (state.error != null) item(key = "stale") { StaleDataBanner(state.error) }
        continueWatching(state.continueWatching, actions.onPlay)
        newEpisodes(days, state.isRefreshing, today, zone, actions.onOpenEpisode)
        ongoing(state.ongoing, state.isRefreshing, actions.onOpenSeries, actions.onShowAllOngoing)
        seasonTop(state.seasonTop, state.isRefreshing, actions.onOpenSeries)
    }
}

/** Блока нет, если продолжать нечего (docs/screens.md). */
private fun LazyListScope.continueWatching(
    cards: List<ContinueWatching>,
    onPlay: (seriesId: Int, episodeId: Int, translationId: Int) -> Unit,
) {
    if (cards.isEmpty()) return
    item(key = "continue-header") { SectionHeader(stringResource(R.string.feature_catalog_continue_watching)) }
    item(key = "continue-row") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = KadrSpacing.Space4),
            horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
        ) {
            items(cards, key = { "continue-${it.seriesId}" }) { card ->
                val last = card.lastWatched
                PosterCard(
                    imageUrl = card.posterUrl,
                    title = card.seriesTitle,
                    meta = last.episodeLabel,
                    score = null,
                    status = null,
                    onClick = { onPlay(card.seriesId, last.episodeId, last.translationId) },
                    modifier = Modifier.width(POSTER_ROW_WIDTH),
                    progress = last.progress.fraction,
                )
            }
        }
    }
}

private fun LazyListScope.newEpisodes(
    days: List<NewEpisodeDay>,
    isRefreshing: Boolean,
    today: LocalDate,
    zone: ZoneId,
    onOpenEpisode: (seriesId: Int, episodeId: Int) -> Unit,
) {
    if (days.isEmpty() && !isRefreshing) return
    item(key = "new-header") { SectionHeader(stringResource(R.string.feature_catalog_new_episodes)) }
    if (days.isEmpty()) {
        items(PLACEHOLDER_ROWS) { ListItemPlaceholder(thumbnailWidth = 40.dp) }
        return
    }
    days.forEach { day ->
        item(key = "day-${day.date}") { DayHeader(day.date, today) }
        items(day.episodes, key = { "episode-${it.episodeId}" }) { episode ->
            NewEpisodeRow(episode, zone, onOpenEpisode)
        }
    }
}

@Composable
private fun NewEpisodeRow(
    episode: NewEpisode,
    zone: ZoneId,
    onOpenEpisode: (seriesId: Int, episodeId: Int) -> Unit,
) {
    EpisodeFeedRow(
        imageUrl = episode.posterUrl,
        episodeLabel = episode.label,
        title = episode.seriesTitle,
        time = TIME_FORMAT.format(episode.releasedAt.atZone(zone)),
        onClick = { onOpenEpisode(episode.seriesId, episode.episodeId) },
    )
}

@Composable
private fun DayHeader(
    date: LocalDate,
    today: LocalDate,
) {
    val label =
        when (date) {
            today -> stringResource(R.string.feature_catalog_today)
            today.minusDays(1) -> stringResource(R.string.feature_catalog_yesterday)
            else -> DATE_FORMAT.format(date)
        }
    Text(
        label.uppercase(),
        Modifier.padding(start = KadrSpacing.Space4, top = KadrSpacing.Space2, bottom = KadrSpacing.Space1),
        style = MaterialTheme.typography.labelSmall,
        color = KadrTheme.palette.inkMuted,
    )
}

private fun LazyListScope.ongoing(
    ongoing: List<SeriesSummary>,
    isRefreshing: Boolean,
    onOpenSeries: (Int) -> Unit,
    onShowAll: () -> Unit,
) {
    if (ongoing.isEmpty() && !isRefreshing) return
    item(key = "ongoing-header") {
        SectionHeader(
            stringResource(R.string.feature_catalog_ongoing),
            onShowAll = onShowAll.takeIf { ongoing.isNotEmpty() },
        )
    }
    item(key = "ongoing-row") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = KadrSpacing.Space4),
            horizontalArrangement = Arrangement.spacedBy(KadrSpacing.Space2),
        ) {
            if (ongoing.isEmpty()) {
                items(PLACEHOLDER_ROWS) { PosterCardPlaceholder(Modifier.width(POSTER_ROW_WIDTH)) }
            } else {
                items(ongoing, key = { it.id }) { series ->
                    SeriesPosterCard(series, onClick = { onOpenSeries(series.id) }, Modifier.width(POSTER_ROW_WIDTH))
                }
            }
        }
    }
}

private fun LazyListScope.seasonTop(
    seasonTop: List<SeriesSummary>,
    isRefreshing: Boolean,
    onOpenSeries: (Int) -> Unit,
) {
    if (seasonTop.isEmpty() && !isRefreshing) return
    item(key = "top-header") {
        SectionHeader(stringResource(R.string.feature_catalog_season_top), Modifier.padding(top = KadrSpacing.Space4))
    }
    if (seasonTop.isEmpty()) {
        items(PLACEHOLDER_ROWS) { ListItemPlaceholder(thumbnailWidth = 56.dp) }
        return
    }
    itemsIndexed(seasonTop, key = { _, series -> "top-${series.id}" }) { index, series ->
        if (index > 0) HorizontalDivider(color = KadrTheme.palette.outlineVariant)
        RankingListItem(
            place = index + 1,
            imageUrl = series.posterUrl,
            title = series.title,
            originalTitle = series.originalTitle,
            score = series.score,
            onClick = { onOpenSeries(series.id) },
        )
    }
}
