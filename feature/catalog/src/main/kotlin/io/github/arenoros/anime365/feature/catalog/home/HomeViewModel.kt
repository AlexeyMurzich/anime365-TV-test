package io.github.arenoros.anime365.feature.catalog.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.arenoros.anime365.core.data.repository.HomeRepository
import io.github.arenoros.anime365.core.model.NewEpisode
import io.github.arenoros.anime365.core.model.SeriesSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

internal data class HomeUiState(
    val newEpisodes: List<NewEpisode> = emptyList(),
    val ongoing: List<SeriesSummary> = emptyList(),
    val seasonTop: List<SeriesSummary> = emptyList(),
    val isRefreshing: Boolean = true,
    /** Ошибка последнего обновления; данные из кэша при этом остаются. */
    val error: Throwable? = null,
) {
    val isEmpty: Boolean get() = newEpisodes.isEmpty() && ongoing.isEmpty() && seasonTop.isEmpty()
}

@HiltViewModel
internal class HomeViewModel
    @Inject
    constructor(
        private val repository: HomeRepository,
    ) : ViewModel() {
        private val refreshing = MutableStateFlow(true)
        private val error = MutableStateFlow<Throwable?>(null)

        val uiState: StateFlow<HomeUiState> =
            combine(repository.newEpisodes, repository.ongoing, repository.seasonTop, refreshing, error) {
                newEpisodes,
                ongoing,
                seasonTop,
                refreshing,
                error,
                ->
                HomeUiState(newEpisodes, ongoing, seasonTop, refreshing, error)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HomeUiState())

        init {
            refresh(force = false)
        }

        /** [force] — потянули вниз: обновить всё, даже свежее. */
        fun refresh(force: Boolean = true) {
            viewModelScope.launch {
                refreshing.value = true
                error.value = repository.refresh(force).exceptionOrNull()
                refreshing.value = false
            }
        }

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
