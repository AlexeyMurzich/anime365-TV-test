package io.github.arenoros.anime365.feature.catalog.catalog

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import io.github.arenoros.anime365.core.data.repository.CatalogRepository
import io.github.arenoros.anime365.core.model.CatalogFilter
import io.github.arenoros.anime365.core.model.Season
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.feature.catalog.MainDispatcherRule
import io.github.arenoros.anime365.feature.catalog.navigation.CatalogKey
import io.github.arenoros.anime365.feature.catalog.summary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeCatalogRepository()

    @Test
    fun keyPresetsFilter() {
        val viewModel = CatalogViewModel(CatalogKey(genreId = 8, ongoingOnly = true), repository)

        assertEquals(CatalogFilter(genreIds = setOf(8), ongoingOnly = true), viewModel.filter.value)
    }

    @Test
    fun searchWaitsUntilTypingPauses() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = CatalogViewModel(CatalogKey.Root, repository)
            backgroundScope.launch { viewModel.series.collect {} }
            runCurrent()

            viewModel.onQueryChange("f")
            advanceTimeBy(100)
            viewModel.onQueryChange("frieren ")
            advanceTimeBy(400)

            assertEquals(listOf("", "frieren"), repository.requests.map { it.first })
        }

    @Test
    fun clearingYearClearsSeason() {
        val viewModel = CatalogViewModel(CatalogKey.Root, repository)

        viewModel.onFilterChange(CatalogFilter(year = 2026, season = Season.Autumn))
        viewModel.onFilterChange(viewModel.filter.value.copy(year = null))

        assertNull(viewModel.filter.value.season)
    }

    @Test
    fun resetClearsQueryAndFilter() {
        val viewModel = CatalogViewModel(CatalogKey(ongoingOnly = true), repository)
        viewModel.onQueryChange("frieren")

        viewModel.reset()

        assertEquals("", viewModel.query.value)
        assertEquals(CatalogFilter(), viewModel.filter.value)
    }

    @Test
    fun exposesRepositoryPages() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.items = listOf(summary(1), summary(2))
            val viewModel = CatalogViewModel(CatalogKey.Root, repository)

            // asSnapshot() на бесконечном потоке из cachedIn не дожидается конца загрузки — берём одну PagingData.
            val page = viewModel.series.first()
            assertEquals(listOf(1, 2), flowOf(page).asSnapshot().map { it.id })
        }

    private class FakeCatalogRepository : CatalogRepository {
        val requests = mutableListOf<Pair<String, CatalogFilter>>()
        var items = emptyList<SeriesSummary>()

        override fun catalog(
            query: String,
            filter: CatalogFilter,
        ): Flow<PagingData<SeriesSummary>> {
            requests += query to filter
            return flowOf(PagingData.from(items))
        }
    }
}
