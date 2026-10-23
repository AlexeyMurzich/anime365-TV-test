package io.github.arenoros.anime365.core.data

import io.github.arenoros.anime365.core.model.ReleaseStatus
import io.github.arenoros.anime365.core.network.model.EpisodeDto
import io.github.arenoros.anime365.core.network.model.LinkDto
import io.github.arenoros.anime365.core.network.model.SeriesDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MappersTest {
    @Test
    fun seriesWithoutTitlesOrHiddenIsSkipped() {
        assertNull(SeriesDto(id = 879, titles = emptyMap()).toEntity())
        assertNull(seriesDto(1).copy(isActive = -1).toEntity())
    }

    @Test
    fun placeholderValuesBecomeNull() {
        val entity =
            SeriesDto(
                id = 1,
                titles = mapOf("romaji" to "Sousou no Frieren"),
                year = 0,
                season = "",
                myAnimeListScore = -1.0,
                numberOfEpisodes = 0,
            ).toEntity()!!

        assertEquals("Sousou no Frieren", entity.title)
        assertNull("второе название не повторяет первое", entity.originalTitle)
        assertNull(entity.year)
        assertNull(entity.season)
        assertNull(entity.score)
        assertNull(entity.episodeCount)
    }

    @Test
    fun releaseStatusFollowsActiveAndAiring() {
        assertEquals(
            ReleaseStatus.Announced,
            seriesDto(1)
                .copy(isActive = 0, isAiring = 1)
                .toEntity()!!
                .toSummary()
                .status,
        )
        assertEquals(ReleaseStatus.Ongoing, seriesDto(1, isAiring = 1).toEntity()!!.toSummary().status)
        assertEquals(ReleaseStatus.Released, seriesDto(1).toEntity()!!.toSummary().status)
    }

    @Test
    fun detailsDropDuplicateLinksAndHiddenEpisodes() {
        val dto =
            seriesDto(1).copy(
                links =
                    listOf(
                        LinkDto("википедия (ja)", "https://ja.wikipedia.org/a"),
                        LinkDto("MyAnimeList", "https://myanimelist.net/anime/1"),
                        LinkDto("википедия (ja)", "https://ja.wikipedia.org/a"),
                    ),
                episodes =
                    listOf(
                        EpisodeDto(id = 10, episodeFull = "1 серия", episodeInt = 1.0, episodeType = "tv"),
                        EpisodeDto(id = 11, episodeFull = "-1 серия", episodeInt = -1.0, isActive = 0),
                        EpisodeDto(id = 12, episodeFull = "1.5 серия", episodeInt = 1.5, episodeType = "tv"),
                    ),
            )

        assertEquals(2, dto.toDetailsEntity(fetchedAt = 0).links.size)
        assertEquals(listOf(10 to 0, 12 to 1), dto.toEpisodeEntities().map { it.id to it.position })
    }
}
