package io.github.arenoros.anime365.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SeriesDetailsTest {
    private val trailer = Episode(1, 1.0, "Трейлер", "preview")
    private val first = Episode(2, 1.0, "1 серия", "tv")
    private val second = Episode(3, 2.0, "2 серия", "tv")
    private val details =
        SeriesDetails("tv", null, null, emptyList(), emptyList(), emptyList(), listOf(trailer, first, second))

    @Test
    fun startsFromFirstMainEpisodeNotTrailer() {
        assertEquals(first, details.firstEpisode)
    }

    @Test
    fun nextEpisodeFollowsSiteOrder() {
        assertEquals(first, details.episodeAfter(trailer.id))
        assertEquals(second, details.episodeAfter(first.id))
        assertNull(details.episodeAfter(second.id))
        assertNull(details.episodeAfter(999))
    }

    @Test
    fun previousEpisodeFollowsSiteOrder() {
        assertEquals(first, details.episodeBefore(second.id))
        assertEquals(trailer, details.episodeBefore(first.id))
        assertNull(details.episodeBefore(trailer.id))
        assertNull(details.episodeBefore(999))
    }
}
