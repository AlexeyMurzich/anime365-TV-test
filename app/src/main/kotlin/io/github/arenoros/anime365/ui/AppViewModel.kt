package io.github.arenoros.anime365.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.arenoros.anime365.core.data.repository.LibraryRepository
import io.github.arenoros.anime365.core.data.repository.RememberedTranslation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** То, что нужно оболочке поверх всех табов: счётчик на табе «Список» и переход из уведомления. */
@HiltViewModel
internal class AppViewModel
    @Inject
    constructor(
        library: LibraryRepository,
        private val rememberedTranslation: RememberedTranslation,
    ) : ViewModel() {
        val newEpisodeCount: StateFlow<Int> =
            library.newEpisodeCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), 0)

        /** `id` Перевода запомненной Команды; `null` — нужен Выбор Перевода. */
        suspend fun rememberedTranslationId(
            seriesId: Int,
            episodeId: Int,
        ): Int? = rememberedTranslation(seriesId, episodeId)?.id

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
