package io.github.arenoros.anime365.feature.catalog.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.arenoros.anime365.core.data.repository.HomeRepository
import io.github.arenoros.anime365.core.data.repository.LibraryRepository
import io.github.arenoros.anime365.core.data.repository.RememberedTranslation
import io.github.arenoros.anime365.core.model.ContinueWatching
import io.github.arenoros.anime365.core.model.FeedEpisode
import io.github.arenoros.anime365.core.model.LibraryStatus
import io.github.arenoros.anime365.core.model.SeriesSummary
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
    val feed: List<FeedEpisode> = emptyList(),
    val ongoing: List<SeriesSummary> = emptyList(),
    val seasonTop: List<SeriesSummary> = emptyList(),
    val isRefreshing: Boolean = true,
    /** Ошибка последнего обновления; данные из кэша при этом остаются. */
    val error: Throwable? = null,
    /** Из Библиотеки на устройстве: виден и без сети. */
    val continueWatching: List<ContinueWatching> = emptyList(),
    /** `id` аниме со Статусом «Смотрю»: их строки в «Новых сериях» выделены. */
    val watching: Set<Int> = emptySet(),
) {
    val isEmpty: Boolean
        get() = continueWatching.isEmpty() && feed.isEmpty() && ongoing.isEmpty() && seasonTop.isEmpty()
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
        library: LibraryRepository,
        private val rememberedTranslation: RememberedTranslation,
    ) : ViewModel() {
        private val eventChannel = Channel<HomeEvent>(Channel.BUFFERED)
        val events: Flow<HomeEvent> = eventChannel.receiveAsFlow()

        private val refreshing = MutableStateFlow(true)
        private val error = MutableStateFlow<Throwable?>(null)

        val uiState: StateFlow<HomeUiState> =
            combine(
                repository.feed,
                repository.ongoing,
                repository.seasonTop,
                combine(refreshing, error, ::Pair),
                combine(library.continueWatching(), library.entries(), ::Pair),
            ) { feed, ongoing, seasonTop, (refreshing, error), (continueWatching, entries) ->
                val watching =
                    entries.filter { it.status == LibraryStatus.Watching }.mapTo(
                        mutableSetOf(),
                    ) { it.seriesId }
                HomeUiState(feed, ongoing, seasonTop, refreshing, error, continueWatching, watching)
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
                val match = rememberedTranslation(seriesId, episodeId)
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
