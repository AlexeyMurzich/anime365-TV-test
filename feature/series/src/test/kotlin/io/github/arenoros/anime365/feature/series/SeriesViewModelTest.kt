package io.github.arenoros.anime365.feature.series

import io.github.arenoros.anime365.core.data.repository.SeriesRepository
import io.github.arenoros.anime365.core.data.repository.SeriesUnavailableException
import io.github.arenoros.anime365.core.model.ReleaseStatus
import io.github.arenoros.anime365.core.model.Series
import io.github.arenoros.anime365.core.model.SeriesSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    @Test
    fun cachedSummaryIsShownWhileDetailsLoad() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.series.value = Series(summary(7), details = null)
            val viewModel = SeriesViewModel(7, repository)
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
            val viewModel = SeriesViewModel(7, repository)
            backgroundScope.launch { viewModel.uiState.collect {} }

            assertTrue(viewModel.uiState.value.error is SeriesUnavailableException)
        }

    @Test
    fun retryForcesRefresh() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = SeriesViewModel(7, repository)

            viewModel.refresh()

            assertEquals(listOf(7 to false, 7 to true), repository.refreshCalls)
        }

    private fun summary(id: Int) =
        SeriesSummary(id, "Фрирен", null, null, null, null, null, null, null, ReleaseStatus.Released)

    private class FakeSeriesRepository : SeriesRepository {
        val series = MutableStateFlow<Series?>(null)
        val refreshCalls = mutableListOf<Pair<Int, Boolean>>()
        var result: Result<Unit> = Result.success(Unit)

        override fun series(id: Int): Flow<Series?> = series

        override suspend fun refresh(
            id: Int,
            force: Boolean,
        ): Result<Unit> {
            refreshCalls += id to force
            return result
        }
    }
}
