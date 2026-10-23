package io.github.arenoros.anime365.feature.catalog.home

import io.github.arenoros.anime365.core.model.FeedEpisode
import java.time.LocalDate
import java.time.ZoneId

internal data class FeedDay(
    val date: LocalDate,
    val episodes: List<FeedEpisode>,
)

/** Группы «Сегодня», «Вчера», «21.10.2026» — по дате выхода в часовом поясе устройства. */
internal fun List<FeedEpisode>.groupByDay(zone: ZoneId): List<FeedDay> =
    groupBy { it.releasedAt.atZone(zone).toLocalDate() }
        .map { (date, episodes) -> FeedDay(date, episodes) }
        .sortedByDescending { it.date }
