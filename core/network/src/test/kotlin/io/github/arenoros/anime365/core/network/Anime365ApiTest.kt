package io.github.arenoros.anime365.core.network

import io.github.arenoros.anime365.core.network.model.ApiException
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/** Разбор ответов, записанных с живого сервера 23.10.2026 (src/test/resources/api). */
class Anime365ApiTest {
    private val server = MockWebServer()
    private lateinit var api: Anime365Api

    @Before
    fun setUp() {
        server.start()
        api = createAnime365Api(server.url("/api/").toString())
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun seriesListSendsFieldsAndUserAgent() =
        runTest {
            enqueue("series-top.json")

            val series = api.series(fields = ApiFields.SERIES_SUMMARY, limit = 3).unwrap()

            assertEquals(listOf(30414, 2400, 39186), series.map { it.id })
            assertEquals("Провожающая в последний путь Фрирен", series[0].titles["ru"])
            assertEquals(9.24, series[0].myAnimeListScore!!, 0.0)
            val request = server.takeRequest()
            assertEquals(ApiFields.SERIES_SUMMARY, request.url.queryParameter("fields"))
            assertEquals("3", request.url.queryParameter("limit"))
            assertNull("пустые параметры не отправляются", request.url.queryParameter("query"))
            assertTrue(request.headers["User-Agent"]!!.startsWith("anime365-android"))
        }

    @Test
    fun emptyTitlesArrayParsesAsEmptyMap() =
        runTest {
            enqueue("series-879-no-titles.json")

            val series = api.series(879, ApiFields.SERIES_SUMMARY).unwrap()

            assertEquals(emptyMap<String, String>(), series.titles)
            assertEquals(0, series.year)
            assertEquals("", series.season)
        }

    @Test
    fun detailsContainGenresDescriptionsLinksAndEpisodes() =
        runTest {
            enqueue("series-30414-details.json")

            val series = api.series(30414, ApiFields.SERIES_DETAILS).unwrap()

            assertEquals(listOf(2, 10, 27, 8, 46), series.genres.map { it.id })
            assertEquals(listOf("shikimori.me", "world-art.ru"), series.descriptions.map { it.source })
            assertTrue(series.links.any { it.title == "MyAnimeList" })
            assertEquals(29, series.episodes.size)
            assertEquals("Трейлер", series.episodes.first().episodeFull)
            assertEquals("preview", series.episodes.first().episodeType)
            assertEquals("https://smotret-anime.co/catalog/sousou-no-frieren-30414", series.url)
        }

    @Test
    fun announcedSeriesHasNoEpisodes() =
        runTest {
            enqueue("series-42431-announced.json")

            val series = api.series(42431, ApiFields.SERIES_DETAILS).unwrap()

            assertEquals(0, series.isActive)
            assertEquals(emptyList<Any>(), series.episodes)
        }

    @Test
    fun feedContainsNestedEpisodes() =
        runTest {
            enqueue("feed-recent.json")

            val feed = api.translations(feed = "recent", fields = ApiFields.FEED, limit = 20).unwrap()

            assertEquals(20, feed.size)
            assertTrue(feed.all { it.episode != null && it.activeDateTime != null })
            assertEquals("recent", server.takeRequest().url.queryParameter("feed"))
        }

    @Test
    fun errorEnvelopeThrowsApiException() =
        runTest {
            enqueue("error-404.json")

            try {
                api.series(999999999, "id").unwrap()
                fail("ожидалась ApiException")
            } catch (e: ApiException) {
                assertEquals(404, e.code)
            }
        }

    private fun enqueue(fixture: String) {
        val body = javaClass.getResource("/api/$fixture")!!.readText()
        server.enqueue(
            MockResponse
                .Builder()
                .body(body)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .build(),
        )
    }
}
