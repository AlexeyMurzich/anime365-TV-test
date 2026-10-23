package io.github.arenoros.anime365.feature.catalog.home

import io.github.arenoros.anime365.feature.catalog.newEpisode
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class FeedDayTest {
    /** 22.10 21:30 UTC — уже 23.10 по Москве: день берётся по часовому поясу устройства. */
    @Test
    fun groupsByLocalDateNewestFirst() {
        val episodes =
            listOf(
                newEpisode(3, "2026-10-23T09:00:00Z"),
                newEpisode(2, "2026-10-22T21:30:00Z"),
                newEpisode(1, "2026-10-22T10:00:00Z"),
            )

        val days = episodes.groupByDay(ZoneId.of("Europe/Moscow"))

        assertEquals(listOf(LocalDate.of(2026, 10, 23), LocalDate.of(2026, 10, 22)), days.map { it.date })
        assertEquals(listOf(3, 2), days[0].episodes.map { it.episodeId })
        assertEquals(listOf(1), days[1].episodes.map { it.episodeId })
    }
}
