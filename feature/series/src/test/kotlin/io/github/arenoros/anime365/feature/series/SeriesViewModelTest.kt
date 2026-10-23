package io.github.arenoros.anime365.feature.series

import io.github.arenoros.anime365.core.data.repository.SeriesUnavailableException
import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.EpisodeProgress
import io.github.arenoros.anime365.core.model.LastWatched
import io.github.arenoros.anime365.core.model.ReleaseStatus
import io.github.arenoros.anime365.core.model.Series
import io.github.arenoros.anime365.core.model.SeriesDetails
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.core.testing.FakeLibraryRepository
import io.github.arenoros.anime365.core.testing.FakeSeriesRepository
import io.github.arenoros.anime365.core.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SeriesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSeriesRepository()
    private val library = FakeLibraryRepository()

    @Test
    fun cachedSummaryIsShownWhileDetailsLoad() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.series.value = Series(summary(7), details = null)
            val viewModel = SeriesViewModel(7, repository, library)
            backgroundScope.launch { viewModel.uiState.collect {} }

            val state = viewModel.uiState.value
            assertEquals(7, state.series?.summary?.id)
            assertNull(state.series?.details)
            assertEquals(listOf(7 to false), repository.refreshCalls)
            assertFalse(state.isRefreshing)
        }

    @Test
    fun unavailableSeriesIsReported() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.result = Result.failure(SeriesUnavailableException(7))
            val viewModel = SeriesViewModel(7, repository, library)
            backgroundScope.launch { viewModel.uiState.collect {} }

            assertTrue(viewModel.uiState.value.error is SeriesUnavailableException)
        }

    @Test
    fun retryForcesRefresh() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = SeriesViewModel(7, repository, library)

            viewModel.refresh()

            assertEquals(listOf(7 to false, 7 to true), repository.refreshCalls)
        }

    @Test
    fun watchActionStartsFromFirstMainEpisode() {
        val state = SeriesUiState(Series(summary(7), details))

        assertEquals(WatchAction.Start(first), state.watchAction)
    }

    @Test
    fun unfinishedEpisodeResumesWithSameTranslation() {
        val last =
            LastWatched(
                first.id,
                first.label,
                translationId = 55,
                EpisodeProgress(first.id, 300, 1000, watched = false),
            )

        val state = SeriesUiState(Series(summary(7), details), lastWatched = last)

        assertEquals(WatchAction.Resume(last), state.watchAction)
    }

    @Test
    fun watchedEpisodeOffersNextOne() {
        val last =
            LastWatched(
                first.id,
                first.label,
                translationId = 55,
                EpisodeProgress(first.id, 1000, 1000, watched = true),
            )

        assertEquals(
            WatchAction.Next(second),
            SeriesUiState(Series(summary(7), details), lastWatched = last).watchAction,
        )
        assertNull(
            SeriesUiState(Series(summary(7), details), lastWatched = last.copy(episodeId = second.id)).watchAction,
        )
    }

    @Test
    fun progressComesFromLibrary() =
        runTest(mainDispatcherRule.dispatcher) {
            library.progress.value = mapOf(first.id to EpisodeProgress(first.id, 10, 100, watched = false))
            val viewModel = SeriesViewModel(7, repository, library)
            backgroundScope.launch { viewModel.uiState.collect {} }

            assertEquals(setOf(first.id), viewModel.uiState.value.progress.keys)
        }

    private val trailer = Episode(1, 1.0, "Трейлер", "preview")
    private val first = Episode(2, 1.0, "1 серия", "tv")
    private val second = Episode(3, 2.0, "2 серия", "tv")
    private val details =
        SeriesDetails("tv", null, null, emptyList(), emptyList(), emptyList(), listOf(trailer, first, second))

    private fun summary(id: Int) =
        SeriesSummary(id, "Фрирен", null, null, null, null, null, null, null, ReleaseStatus.Released)
}
