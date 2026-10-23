package io.github.arenoros.anime365.core.data.repository

import io.github.arenoros.anime365.core.data.FakeAnime365Api
import io.github.arenoros.anime365.core.data.MutableClock
import io.github.arenoros.anime365.core.data.inMemoryCacheDatabase
import io.github.arenoros.anime365.core.data.seriesDto
import io.github.arenoros.anime365.core.database.CacheDatabase
import io.github.arenoros.anime365.core.network.model.DescriptionDto
import io.github.arenoros.anime365.core.network.model.EpisodeDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SeriesRepositoryTest {
    private val database: CacheDatabase = inMemoryCacheDatabase()
    private val api = FakeAnime365Api()
    private val clock = MutableClock()
    private val repository = OfflineFirstSeriesRepository(api, database.seriesDao(), clock)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun refreshStoresDetailsAndEpisodesInSiteOrder() =
        runTest {
            api.seriesById = { id ->
                seriesDto(id).copy(
                    descriptions =
                        listOf(
                            DescriptionDto("shikimori.me", "  Текст  "),
                            DescriptionDto("world-art.ru", ""),
                        ),
                    episodes =
                        listOf(
                            EpisodeDto(id = 5, episodeFull = "Трейлер", episodeInt = 1.0, episodeType = "preview"),
                            EpisodeDto(id = 3, episodeFull = "1 серия", episodeInt = 1.0, episodeType = "tv"),
                        ),
                )
            }
            assertNull(repository.series(7).first())

            assertTrue(repository.refresh(7, force = false).isSuccess)

            val details = repository.series(7).first()!!.details!!
            assertEquals(listOf("Трейлер", "1 серия"), details.episodes.map { it.label })
            assertEquals(listOf("Текст"), details.descriptions.map { it.text })
            assertEquals("tv", details.type)
        }

    @Test
    fun detailsAreCachedForOneDay() =
        runTest {
            api.seriesById = { seriesDto(it) }
            repository.refresh(7, force = false)

            clock.advanceMinutes(23 * 60)
            repository.refresh(7, force = false)
            assertEquals(1, api.detailsRequests.size)

            repository.refresh(7, force = true)
            assertEquals(2, api.detailsRequests.size)
        }

    @Test
    fun hiddenSeriesIsReportedAsUnavailable() =
        runTest {
            api.seriesById = { seriesDto(it).copy(isActive = -2) }

            val result = repository.refresh(7, force = false)

            assertTrue(result.exceptionOrNull() is SeriesUnavailableException)
        }
}
