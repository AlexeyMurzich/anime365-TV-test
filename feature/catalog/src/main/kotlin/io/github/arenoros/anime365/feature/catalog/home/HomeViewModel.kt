package io.github.arenoros.anime365.feature.catalog.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.arenoros.anime365.core.data.repository.HomeRepository
import io.github.arenoros.anime365.core.data.repository.LibraryRepository
import io.github.arenoros.anime365.core.data.repository.TranslationRepository
import io.github.arenoros.anime365.core.model.ContinueWatching
import io.github.arenoros.anime365.core.model.NewEpisode
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.core.model.ofTeam
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
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
    /** Из Библиотеки на устройстве: виден и без сети. */
    val continueWatching: List<ContinueWatching> = emptyList(),
) {
    val isEmpty: Boolean
        get() = continueWatching.isEmpty() && newEpisodes.isEmpty() && ongoing.isEmpty() && seasonTop.isEmpty()
}

/** Куда ведёт касание Новой серии (docs/screens.md, Главная). */
internal sealed interface HomeEvent {
    data class Play(
        val seriesId: Int,
        val episodeId: Int,
        val translationId: Int,
    ) : HomeEvent

    data class ChooseTranslation(
        val seriesId: Int,
        val episodeId: Int,
    ) : HomeEvent
}

@HiltViewModel
internal class HomeViewModel
    @Inject
    constructor(
        private val repository: HomeRepository,
        private val library: LibraryRepository,
        private val translations: TranslationRepository,
    ) : ViewModel() {
        private val eventChannel = Channel<HomeEvent>(Channel.BUFFERED)
        val events: Flow<HomeEvent> = eventChannel.receiveAsFlow()

        private val refreshing = MutableStateFlow(true)
        private val error = MutableStateFlow<Throwable?>(null)

        val uiState: StateFlow<HomeUiState> =
            combine(
                repository.newEpisodes,
                repository.ongoing,
                repository.seasonTop,
                combine(refreshing, error, ::Pair),
                library.continueWatching(),
            ) { newEpisodes, ongoing, seasonTop, (refreshing, error), continueWatching ->
                HomeUiState(newEpisodes, ongoing, seasonTop, refreshing, error, continueWatching)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), HomeUiState())

        init {
            refresh(force = false)
        }

        /** Плеер с Переводом запомненной Команды; если её у этой серии нет — Выбор Перевода. */
        fun openEpisode(
            seriesId: Int,
            episodeId: Int,
        ) {
            viewModelScope.launch {
                val choice = library.choice(seriesId)
                val match = choice?.let { translations.load(episodeId).getOrNull()?.ofTeam(it) }
                eventChannel.send(
                    if (match != null) {
                        HomeEvent.Play(seriesId, episodeId, match.id)
                    } else {
                        HomeEvent.ChooseTranslation(seriesId, episodeId)
                    },
                )
            }
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
