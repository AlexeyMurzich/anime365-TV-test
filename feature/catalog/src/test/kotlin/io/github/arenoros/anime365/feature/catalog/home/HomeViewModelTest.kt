package io.github.arenoros.anime365.feature.catalog.home

import io.github.arenoros.anime365.core.data.repository.HomeRepository
import io.github.arenoros.anime365.core.model.NewEpisode
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.feature.catalog.MainDispatcherRule
import io.github.arenoros.anime365.feature.catalog.summary
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeHomeRepository()

    @Test
    fun opensWithRefreshThatRespectsCacheAge() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = HomeViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }

            assertTrue(viewModel.uiState.value.isRefreshing)
            repository.result.complete(Result.success(Unit))

            assertEquals(listOf(false), repository.refreshCalls)
            assertFalse(viewModel.uiState.value.isRefreshing)
        }

    @Test
    fun showsCachedDataWithErrorWhenRefreshFails() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.ongoing.value = listOf(summary(1))
            val viewModel = HomeViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }

            repository.result.complete(Result.failure(IOException("offline")))

            val state = viewModel.uiState.value
            assertEquals(listOf(1), state.ongoing.map { it.id })
            assertTrue(state.error is IOException)
            assertFalse(state.isEmpty)
        }

    @Test
    fun pullToRefreshForcesAndClearsOldError() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.result.complete(Result.failure(IOException("offline")))
            val viewModel = HomeViewModel(repository)
            backgroundScope.launch { viewModel.uiState.collect {} }

            repository.result = CompletableDeferred(Result.success(Unit))
            viewModel.refresh()

            assertEquals(listOf(false, true), repository.refreshCalls)
            assertNull(viewModel.uiState.value.error)
        }

    private class FakeHomeRepository : HomeRepository {
        override val newEpisodes = MutableStateFlow(emptyList<NewEpisode>())
        override val ongoing = MutableStateFlow(emptyList<SeriesSummary>())
        override val seasonTop = MutableStateFlow(emptyList<SeriesSummary>())
        val refreshCalls = mutableListOf<Boolean>()
        var result = CompletableDeferred<Result<Unit>>()

        override suspend fun refresh(force: Boolean): Result<Unit> {
            refreshCalls += force
            return result.await()
        }
    }
}
