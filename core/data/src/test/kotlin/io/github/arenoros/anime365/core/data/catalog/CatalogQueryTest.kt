package io.github.arenoros.anime365.core.data.catalog

import io.github.arenoros.anime365.core.data.seriesDto
import io.github.arenoros.anime365.core.model.CatalogFilter
import io.github.arenoros.anime365.core.model.GenreMatch
import io.github.arenoros.anime365.core.model.Season
import io.github.arenoros.anime365.core.model.SeriesType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogQueryTest {
    @Test
    fun emptyFilterHasNoChips() {
        assertEquals("", CatalogFilter().toChips())
    }

    /** Строки сверены с живым API 23.10.2026 (docs/api-notes.md). */
    @Test
    fun chipsFollowSiteSyntax() {
        assertEquals(
            "genre@=8,35;type=tv;yearseason=2015;genre_op=and",
            CatalogFilter(types = setOf(SeriesType.Tv), year = 2015, genreIds = setOf(35, 8)).toChips(),
        )
        assertEquals(
            "genre=8,35;genre_op=or",
            CatalogFilter(genreIds = setOf(8, 35), genreMatch = GenreMatch.Any).toChips(),
        )
        assertEquals(
            "type=tv,movie,special,tv_special;yearseason=autumn_2026;isAiring=1",
            CatalogFilter(
                types = setOf(SeriesType.Special, SeriesType.Tv, SeriesType.Movie),
                year = 2026,
                season = Season.Autumn,
                ongoingOnly = true,
            ).toChips(),
        )
    }

    @Test
    fun seasonWithoutYearIsIgnored() {
        assertEquals("", CatalogFilter(season = Season.Winter).toChips())
    }

    @Test
    fun singleGenreHasNoOperator() {
        assertEquals("genre@=8", CatalogFilter(genreIds = setOf(8)).toChips())
    }

    @Test
    fun searchKeyDependsOnTextAndFilter() {
        val plain = CatalogQuery("", CatalogFilter())
        val search = CatalogQuery("Frieren", CatalogFilter())
        val filtered = CatalogQuery("Frieren", CatalogFilter(ongoingOnly = true))

        assertFalse(plain.isSearch)
        assertTrue(search.isSearch)
        assertEquals(CatalogQuery("frieren", CatalogFilter()).listKey, search.listKey)
        assertNotEquals(search.listKey, filtered.listKey)
        assertNotEquals(plain.listKey, search.listKey)
    }

    @Test
    fun searchMatchesTypeYearAndOngoing() {
        val query = CatalogQuery("x", CatalogFilter(types = setOf(SeriesType.Special), year = 2026, ongoingOnly = true))

        assertTrue(query.matches(seriesDto(1, type = "tv_special", isAiring = 1)))
        assertFalse(query.matches(seriesDto(2, type = "tv", isAiring = 1)))
        assertFalse(query.matches(seriesDto(3, type = "special", year = 2025, isAiring = 1)))
        assertFalse(query.matches(seriesDto(4, type = "special", isAiring = 0)))
    }

    /** «Зима 2016» у сайта — аниме с `year = 2015`: сезон сравнивается по строке сезона, год — по `year`. */
    @Test
    fun searchMatchesSeasonBySiteSeasonString() {
        val winter = CatalogQuery("x", CatalogFilter(year = 2016, season = Season.Winter))
        val year2015 = CatalogQuery("x", CatalogFilter(year = 2015))
        val series = seriesDto(1, year = 2015, season = "Зима 2016")

        assertTrue(winter.matches(series))
        assertTrue(year2015.matches(series))
    }

    @Test
    fun searchMatchesGenresAllOrAny() {
        val series = seriesDto(1, genres = listOf(8, 10))

        assertTrue(CatalogQuery("x", CatalogFilter(genreIds = setOf(8, 10))).matches(series))
        assertFalse(CatalogQuery("x", CatalogFilter(genreIds = setOf(8, 35))).matches(series))
        assertTrue(
            CatalogQuery("x", CatalogFilter(genreIds = setOf(8, 35), genreMatch = GenreMatch.Any)).matches(series),
        )
    }
}
