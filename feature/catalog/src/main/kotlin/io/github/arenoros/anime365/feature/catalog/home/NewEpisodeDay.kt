package io.github.arenoros.anime365.feature.catalog.home

import io.github.arenoros.anime365.core.model.NewEpisode
import java.time.LocalDate
import java.time.ZoneId

internal data class NewEpisodeDay(
    val date: LocalDate,
    val episodes: List<NewEpisode>,
)

/** Группы «Сегодня», «Вчера», «21.10.2026» — по дате выхода в часовом поясе устройства. */
internal fun List<NewEpisode>.groupByDay(zone: ZoneId): List<NewEpisodeDay> =
    groupBy { it.releasedAt.atZone(zone).toLocalDate() }
        .map { (date, episodes) -> NewEpisodeDay(date, episodes) }
        .sortedByDescending { it.date }
