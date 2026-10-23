package io.github.arenoros.anime365.feature.catalog.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.arenoros.anime365.core.data.repository.CatalogRepository
import io.github.arenoros.anime365.core.model.CatalogFilter
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.feature.catalog.navigation.CatalogKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@HiltViewModel(assistedFactory = CatalogViewModel.Factory::class)
internal class CatalogViewModel
    @AssistedInject
    constructor(
        @Assisted key: CatalogKey,
        repository: CatalogRepository,
    ) : ViewModel() {
        private val _query = MutableStateFlow("")
        val query: StateFlow<String> = _query.asStateFlow()

        private val _filter =
            MutableStateFlow(CatalogFilter(genreIds = setOfNotNull(key.genreId), ongoingOnly = key.ongoingOnly))
        val filter: StateFlow<CatalogFilter> = _filter.asStateFlow()

        /** Поиск по мере ввода: запрос уходит, когда пользователь замер на [SEARCH_DEBOUNCE_MILLIS]. */
        @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
        val series: Flow<PagingData<SeriesSummary>> =
            combine(
                _query
                    .debounce { if (it.isBlank()) 0L else SEARCH_DEBOUNCE_MILLIS }
                    .map { it.trim() }
                    .distinctUntilChanged(),
                _filter,
            ) { query, filter -> query to filter }
                .flatMapLatest { (query, filter) -> repository.catalog(query, filter) }
                .cachedIn(viewModelScope)

        fun onQueryChange(query: String) {
            _query.value = query
        }

        /** Сезон без года API не ищет (docs/api-notes.md): снимаем год — снимается и сезон. */
        fun onFilterChange(filter: CatalogFilter) {
            _filter.value = if (filter.year == null) filter.copy(season = null) else filter
        }

        fun reset() {
            _query.value = ""
            _filter.value = CatalogFilter()
        }

        @AssistedFactory
        interface Factory {
            fun create(key: CatalogKey): CatalogViewModel
        }

        private companion object {
            const val SEARCH_DEBOUNCE_MILLIS = 300L
        }
    }
