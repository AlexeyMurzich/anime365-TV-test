package io.github.arenoros.anime365.feature.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.arenoros.anime365.core.data.repository.SeriesRepository
import io.github.arenoros.anime365.core.model.Series
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal data class SeriesUiState(
    /** Из кэша: шапка видна сразу, если аниме уже встречалось в списке. */
    val series: Series? = null,
    val isRefreshing: Boolean = true,
    val error: Throwable? = null,
)

@HiltViewModel(assistedFactory = SeriesViewModel.Factory::class)
internal class SeriesViewModel
    @AssistedInject
    constructor(
        @Assisted private val seriesId: Int,
        private val repository: SeriesRepository,
    ) : ViewModel() {
        private val refreshing = MutableStateFlow(true)
        private val error = MutableStateFlow<Throwable?>(null)

        val uiState: StateFlow<SeriesUiState> =
            combine(repository.series(seriesId), refreshing, error, ::SeriesUiState)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SeriesUiState())

        init {
            refresh(force = false)
        }

        fun refresh(force: Boolean = true) {
            viewModelScope.launch {
                refreshing.value = true
                error.value = repository.refresh(seriesId, force).exceptionOrNull()
                refreshing.value = false
            }
        }

        @AssistedFactory
        interface Factory {
            fun create(seriesId: Int): SeriesViewModel
        }

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
