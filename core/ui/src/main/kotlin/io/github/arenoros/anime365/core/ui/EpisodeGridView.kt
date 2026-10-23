package io.github.arenoros.anime365.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.arenoros.anime365.core.designsystem.component.EmptyState
import io.github.arenoros.anime365.core.designsystem.component.EpisodeButton
import io.github.arenoros.anime365.core.designsystem.component.EpisodeState
import io.github.arenoros.anime365.core.designsystem.theme.episodeColumns
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.EpisodeProgress
import io.github.arenoros.anime365.core.model.SeriesDetails

private const val PERCENT = 100

/**
 * EpisodeGrid Kadr: трейлеры, спешлы и фильмы — широкими кнопками, нумерованные — сеткой, от
 * 100 Эпизодов — диапазонами. [progress] — Прогресс по `id` Эпизода. [currentEpisodeId] — что играет
 * сейчас: в Плеере сетка открывается на нём. [newEpisodes] — `id` Новых Эпизодов.
 * [onEpisodeLongClick] — действия с Эпизодом (отметить просмотренным); `null` — их нет.
 */
@Composable
fun EpisodeGrid(
    details: SeriesDetails,
    episodeCount: Int?,
    ongoing: Boolean,
    progress: Map<Int, EpisodeProgress>,
    onEpisodeClick: (Episode) -> Unit,
    modifier: Modifier = Modifier,
    currentEpisodeId: Int? = null,
    newEpisodes: Set<Int> = emptySet(),
    onEpisodeLongClick: ((Episode) -> Unit)? = null,
) {
    val grid = details.episodeGrid(episodeCount)
    val marks = EpisodeMarks(progress, currentEpisodeId, newEpisodes)
    val actionsLabel = stringResource(R.string.core_ui_episode_actions)
    if (grid.special.isEmpty() && grid.numbered.isEmpty()) {
        EmptyState(stringResource(R.string.core_ui_no_episodes), modifier)
        return
    }
    BoxWithConstraints(modifier) {
        val columns = episodeColumns(maxWidth)
        val wideColumns = (columns / 2).coerceAtLeast(1)
        Column {
            grid.special.chunked(wideColumns).forEach { row ->
                GridRow {
                    row.forEach { episode ->
                        EpisodeButton(
                            label = episode.label,
                            description = marks.description(episode),
                            state = marks.state(episode),
                            onClick = { onEpisodeClick(episode) },
                            modifier = Modifier.weight(2f),
                            wide = true,
                            progress = progress[episode.id]?.fraction ?: 0f,
                            onLongClick = onEpisodeLongClick?.let { { it(episode) } },
                            onLongClickLabel = actionsLabel,
                        )
                    }
                    repeat(wideColumns - row.size) { Spacer(Modifier.weight(2f)) }
                }
            }
            var range by rememberSaveable { mutableIntStateOf(grid.initialRange(ongoing, currentEpisodeId)) }
            if (grid.ranges.isNotEmpty()) RangeChips(grid, range, onSelect = { range = it })
            val cells = grid.ranges.getOrNull(range)?.let { grid.numbered.slice(it) } ?: grid.numbered
            cells.chunked(columns).forEach { row ->
                GridRow {
                    row.forEach { cell ->
                        NumberedEpisodeButton(cell, marks, onEpisodeClick, onEpisodeLongClick, Modifier.weight(1f))
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** Диапазон с текущим Эпизодом; у онгоинга без него — последний, там новые серии. */
private fun EpisodeGrid.initialRange(
    ongoing: Boolean,
    currentEpisodeId: Int?,
): Int {
    val current = numbered.indexOfFirst { it is EpisodeCell.Released && it.episode.id == currentEpisodeId }
    return when {
        current >= 0 -> ranges.indexOfFirst { current in it }.coerceAtLeast(0)
        ongoing -> ranges.lastIndex.coerceAtLeast(0)
        else -> 0
    }
}

/** Что известно об Эпизодах помимо самой сетки: Прогресс, что играет, что вышло нового. */
private class EpisodeMarks(
    val progress: Map<Int, EpisodeProgress>,
    val currentEpisodeId: Int?,
    val newEpisodes: Set<Int>,
) {
    fun state(episode: Episode): EpisodeState {
        val episodeProgress = progress[episode.id]
        return when {
            episode.id == currentEpisodeId -> EpisodeState.Current
            episodeProgress?.watched == true -> EpisodeState.Watched
            episodeProgress != null && episodeProgress.positionMs > 0 -> EpisodeState.Current
            episode.id in newEpisodes -> EpisodeState.New
            else -> EpisodeState.Released
        }
    }

    @Composable
    fun description(episode: Episode): String {
        val episodeProgress = progress[episode.id]
        return when (state(episode)) {
            EpisodeState.Watched -> {
                stringResource(R.string.core_ui_episode_watched, episode.label)
            }

            EpisodeState.Current if episodeProgress != null -> {
                stringResource(
                    R.string.core_ui_episode_current,
                    episode.label,
                    (episodeProgress.fraction * PERCENT).toInt(),
                )
            }

            EpisodeState.New -> {
                stringResource(R.string.core_ui_episode_new, episode.label)
            }

            else -> {
                episode.label
            }
        }
    }
}

@Composable
private fun NumberedEpisodeButton(
    cell: EpisodeCell,
    marks: EpisodeMarks,
    onEpisodeClick: (Episode) -> Unit,
    onEpisodeLongClick: ((Episode) -> Unit)?,
    modifier: Modifier,
) {
    when (cell) {
        is EpisodeCell.Released -> {
            EpisodeButton(
                label = cell.number,
                description = marks.description(cell.episode),
                state = marks.state(cell.episode),
                onClick = { onEpisodeClick(cell.episode) },
                modifier = modifier,
                progress = marks.progress[cell.episode.id]?.fraction ?: 0f,
                onLongClick = onEpisodeLongClick?.let { { it(cell.episode) } },
                onLongClickLabel = stringResource(R.string.core_ui_episode_actions),
            )
        }

        is EpisodeCell.Upcoming -> {
            EpisodeButton(
                label = cell.number,
                description = stringResource(R.string.core_ui_episode_upcoming, cell.number),
                state = EpisodeState.Upcoming,
                onClick = null,
                modifier = modifier,
            )
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
                    R.string.core_ui_episode_range,
                    grid.numbered[range.first].number,
                    grid.numbered[range.last].number,
                )
            FilterChip(selected = index == selected, onClick = { onSelect(index) }, label = { Text(label) })
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
