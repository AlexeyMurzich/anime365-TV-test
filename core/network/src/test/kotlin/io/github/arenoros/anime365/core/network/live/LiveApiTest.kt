package io.github.arenoros.anime365.core.network.live

import io.github.arenoros.anime365.core.network.ApiFields
import io.github.arenoros.anime365.core.network.createAnime365Api
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Против живого сервера: ловит изменения API, которых не видят записанные ответы.
 * В CI не запускается; локально — `./gradlew :core:network:testDebugUnitTest -PliveApi`.
 */
class LiveApiTest {
    private val api = createAnime365Api()

    @Test
    fun catalogIsSortedByScore() =
        runTest {
            val series = api.series(fields = ApiFields.SERIES_SUMMARY, limit = 20).unwrap()

            val scores = series.mapNotNull { it.myAnimeListScore }
            assertEquals(20, series.size)
            assertEquals(scores.sortedDescending(), scores)
        }

    @Test
    fun chipsOverrideQuery() =
        runTest {
            val withChips =
                api.series(fields = "id", limit = 50, query = "frieren", chips = "isAiring=1").unwrap()

            assertTrue("сервер начал сочетать query и chips: пересмотреть поиск в Каталоге", withChips.size > 10)
        }

    @Test
    fun detailsAndFeedParse() =
        runTest {
            val series = api.series(30414, ApiFields.SERIES_DETAILS).unwrap()
            val feed = api.translations(feed = "recent", fields = ApiFields.FEED, limit = 50).unwrap()

            assertTrue(series.episodes.isNotEmpty())
            assertTrue(feed.all { it.episode != null })
        }

    @Test
    fun streamNeedsTokenAndHasSubtitles() =
        runTest {
            val token = System.getProperty("anime365.accessToken").orEmpty()
            assumeTrue("нет Токена в secrets.properties", token.isNotEmpty())

            // 9volt, 1-я серия Фрирен: Отдельные субтитры (docs/api-notes.md).
            val embed = api.embed(5987619, token).unwrap()
            val user = api.me(token).unwrap()

            assertTrue(embed.stream.isNotEmpty() && embed.stream.all { it.urls.isNotEmpty() })
            assertTrue(embed.subtitlesUrl!!.startsWith("/"))
            assertTrue(user.isLogined)
        }
}
