package io.github.arenoros.anime365.core.data

import io.github.arenoros.anime365.core.model.Season
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** Даты в API — московское время без зоны: `2026-10-23 14:50:51`. */
private val SITE_ZONE: ZoneId = ZoneId.of("Europe/Moscow")
private val SITE_DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

internal fun parseSiteDateTime(value: String?): Instant? =
    value?.let {
        try {
            LocalDateTime.parse(it, SITE_DATE_TIME).atZone(SITE_ZONE).toInstant()
        } catch (_: DateTimeParseException) {
            null
        }
    }

/** Сезон в том виде, в каком его хранит сайт: «Осень 2026». */
internal fun siteSeason(
    season: Season,
    year: Int,
): String {
    val name =
        when (season) {
            Season.Winter -> "Зима"
            Season.Spring -> "Весна"
            Season.Summer -> "Лето"
            Season.Autumn -> "Осень"
        }
    return "$name $year"
}

@Suppress("MagicNumber")
internal fun currentSiteSeason(clock: Clock): String {
    val today = LocalDate.now(clock.withZone(SITE_ZONE))
    val season =
        when (today.monthValue) {
            in 1..3 -> Season.Winter
            in 4..6 -> Season.Spring
            in 7..9 -> Season.Summer
            else -> Season.Autumn
        }
    return siteSeason(season, today.year)
}
