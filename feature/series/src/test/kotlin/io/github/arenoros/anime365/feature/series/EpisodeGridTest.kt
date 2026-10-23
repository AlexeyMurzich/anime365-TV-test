package io.github.arenoros.anime365.feature.series

import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.SeriesDetails
import org.junit.Assert.assertEquals
import org.junit.Test

class EpisodeGridTest {
    @Test
    fun onlyMainTypeIsNumbered() {
        val grid =
            details(
                type = "tv",
                Episode(1, 1.0, "Трейлер", "preview"),
                Episode(2, 1.0, "1 серия", "tv"),
                Episode(3, 1.5, "1.5 серия", "tv"),
                Episode(4, 1.0, "OVA 1 серия", "ova"),
            ).episodeGrid(episodeCount = null)

        assertEquals(listOf("Трейлер", "OVA 1 серия"), grid.special.map { it.label })
        assertEquals(listOf("1", "1.5"), grid.numbered.map { it.number })
    }

    @Test
    fun episodesUpToCountAreUpcoming() {
        val grid = details("tv", Episode(1, 1.0, "1 серия", "tv"), Episode(2, 2.0, "2 серия", "tv")).episodeGrid(4)

        assertEquals(listOf("1", "2", "3", "4"), grid.numbered.map { it.number })
        assertEquals(listOf(false, false, true, true), grid.numbered.map { it is EpisodeCell.Upcoming })
    }

    @Test
    fun movieSeriesHasNoNumberedGrid() {
        val grid = details("movie", Episode(1, 1.0, "Фильм", "movie")).episodeGrid(1)

        assertEquals(listOf("Фильм"), grid.special.map { it.label })
        assertEquals(emptyList<EpisodeCell>(), grid.numbered)
    }

    @Test
    fun rangesOfFiftyFromHundredEpisodes() {
        val few = details("tv", *(1..99).map { Episode(it, it.toDouble(), "$it серия", "tv") }.toTypedArray())
        val many = details("tv", *(1..120).map { Episode(it, it.toDouble(), "$it серия", "tv") }.toTypedArray())

        assertEquals(emptyList<IntRange>(), few.episodeGrid(null).ranges)
        assertEquals(listOf(0..49, 50..99, 100..119), many.episodeGrid(null).ranges)
    }

    private fun details(
        type: String,
        vararg episodes: Episode,
    ) = SeriesDetails(
        type = type,
        largePosterUrl = null,
        siteUrl = null,
        genres = emptyList(),
        descriptions = emptyList(),
        links = emptyList(),
        episodes = episodes.toList(),
    )
}
