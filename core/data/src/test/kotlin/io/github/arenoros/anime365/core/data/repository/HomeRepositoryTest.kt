package io.github.arenoros.anime365.core.data.repository

import io.github.arenoros.anime365.core.data.FakeAnime365Api
import io.github.arenoros.anime365.core.data.MutableClock
import io.github.arenoros.anime365.core.data.inMemoryCacheDatabase
import io.github.arenoros.anime365.core.data.seriesDto
import io.github.arenoros.anime365.core.database.CacheDatabase
import io.github.arenoros.anime365.core.network.model.EpisodeDto
import io.github.arenoros.anime365.core.network.model.TranslationDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class HomeRepositoryTest {
    private val database: CacheDatabase = inMemoryCacheDatabase()
    private val api = FakeAnime365Api()
    private val clock = MutableClock()
    private val repository =
        OfflineFirstHomeRepository(api, database.seriesDao(), database.seriesListDao(), database.feedDao(), clock)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun refreshFillsAllBlocks() =
        runTest {
            api.seriesList = { request ->
                when {
                    request.isAiring == 1 -> listOf(seriesDto(1, isAiring = 1), seriesDto(2, isAiring = 1))
                    request.season != null -> listOf(seriesDto(3, score = 8.5), seriesDto(4, score = -1.0))
                    else -> error("unexpected $request")
                }
            }

            assertTrue(repository.refresh(force = false).isSuccess)

            assertEquals(listOf(1, 2), repository.ongoing.first().map { it.id })
            assertEquals("Осень 2026", api.seriesRequests.first { it.season != null }.season)
            assertEquals("аниме без оценки не входят в рейтинг", listOf(3), repository.seasonTop.first().map { it.id })
        }

    /** Окно Ленты — с 10:00 до 14:00 по Москве; Перевод к серии от 2024 года её новой не делает. */
    @Test
    fun feedBecomesOneRowPerNewEpisode() =
        runTest {
            api.seriesList =
                { request -> if (request.isAiring == 1) listOf(seriesDto(1, title = "Фрирен")) else emptyList() }
            api.seriesById = { id -> seriesDto(id, title = "Не онгоинг") }
            api.feed = {
                listOf(
                    translation(
                        1,
                        seriesId = 1,
                        active = "2026-10-23 14:00:00",
                        episode(11, 1, uploaded = "2026-10-23 13:00:00"),
                    ),
                    translation(
                        2,
                        seriesId = 1,
                        active = "2026-10-23 13:30:00",
                        episode(11, 1, uploaded = "2026-10-23 13:00:00"),
                    ),
                    translation(
                        3,
                        seriesId = 2,
                        active = "2026-10-23 12:00:00",
                        episode(21, 2, uploaded = "2026-10-23 11:00:00"),
                    ),
                    translation(
                        4,
                        seriesId = 1,
                        active = "2026-10-23 10:00:00",
                        episode(9, 1, uploaded = "2024-01-01 10:00:00"),
                    ),
                )
            }

            repository.refresh(force = false)

            val episodes = repository.newEpisodes.first()
            assertEquals(listOf(11, 21), episodes.map { it.episodeId })
            assertEquals(listOf("Фрирен", "Не онгоинг"), episodes.map { it.seriesTitle })
            assertEquals(Instant.parse("2026-10-23T10:00:00Z"), episodes.first().releasedAt)
            assertEquals("аниме не из онгоингов догружается по одному", listOf(2), api.detailsRequests)
        }

    @Test
    fun freshBlocksAreNotRequestedAgainUntilForced() =
        runTest {
            repository.refresh(force = false)
            val requests = api.seriesRequests.size + api.feedRequests

            clock.advanceMinutes(10)
            repository.refresh(force = false)
            assertEquals(requests, api.seriesRequests.size + api.feedRequests)

            repository.refresh(force = true)
            assertEquals(requests * 2, api.seriesRequests.size + api.feedRequests)
        }

    @Test
    fun feedFailureKeepsOtherBlocksAndIsReported() =
        runTest {
            api.seriesList =
                { request -> if (request.isAiring == 1) listOf(seriesDto(1, isAiring = 1)) else emptyList() }
            api.feed = { throw IOException("offline") }

            val result = repository.refresh(force = false)

            assertTrue(result.exceptionOrNull() is IOException)
            assertEquals(listOf(1), repository.ongoing.first().map { it.id })
        }

    private fun translation(
        id: Int,
        seriesId: Int,
        active: String,
        episode: EpisodeDto,
    ) = TranslationDto(id = id, seriesId = seriesId, activeDateTime = active, episode = episode)

    private fun episode(
        id: Int,
        seriesId: Int,
        uploaded: String,
    ) = EpisodeDto(
        id = id,
        seriesId = seriesId,
        episodeFull = "$id серия",
        episodeInt = id.toDouble(),
        episodeType = "tv",
        firstUploadedDateTime = uploaded,
    )
}
