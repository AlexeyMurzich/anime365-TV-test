package io.github.arenoros.anime365.feature.series.translations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.arenoros.anime365.core.data.repository.AccountRepository
import io.github.arenoros.anime365.core.data.repository.LibraryRepository
import io.github.arenoros.anime365.core.data.repository.SeriesRepository
import io.github.arenoros.anime365.core.data.repository.TranslationRepository
import io.github.arenoros.anime365.core.model.Account
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationChoice
import io.github.arenoros.anime365.core.model.TranslationKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock

/** Что будет по касанию Перевода: Плеер, Вход или Нужна Подписка. Список виден всем (docs/screens.md). */
internal enum class PlaybackAccess { Allowed, NeedsLogin, NeedsSubscription }

internal data class TranslationPickerUiState(
    val episodeLabel: String? = null,
    val translations: List<Translation> = emptyList(),
    /** Выбранный пользователем Вид; `null` — по умолчанию, см. [selectedKind]. */
    val pickedKind: TranslationKind? = null,
    val choice: TranslationChoice? = null,
    val access: PlaybackAccess = PlaybackAccess.Allowed,
    val isRefreshing: Boolean = true,
    val error: Throwable? = null,
) {
    /** Только Виды, которые есть у этого Эпизода, в порядке Выбора Перевода. */
    val kinds: List<TranslationKind> get() = translations.map { it.kind }.distinct().sorted()

    /** Запомненный для аниме Вид, иначе русская озвучка, иначе первый, что есть. */
    val selectedKind: TranslationKind?
        get() =
            pickedKind?.takeIf { it in kinds }
                ?: choice?.kind?.takeIf { it in kinds }
                ?: TranslationKind.VoiceRu.takeIf { it in kinds }
                ?: kinds.firstOrNull()

    val shown: List<Translation> get() = translations.filter { it.kind == selectedKind }

    /** Перевод той Команды, которой смотрели прошлый Эпизод. */
    fun isRemembered(translation: Translation): Boolean =
        choice != null && translation.kind == choice.kind && translation.team == choice.team
}

@HiltViewModel(assistedFactory = TranslationPickerViewModel.Factory::class)
internal class TranslationPickerViewModel
    @AssistedInject
    constructor(
        @Assisted("seriesId") private val seriesId: Int,
        @Assisted("episodeId") private val episodeId: Int,
        seriesRepository: SeriesRepository,
        private val translationRepository: TranslationRepository,
        private val libraryRepository: LibraryRepository,
        accountRepository: AccountRepository,
        private val clock: Clock,
    ) : ViewModel() {
        private val pickedKind = MutableStateFlow<TranslationKind?>(null)
        private val choice = MutableStateFlow<TranslationChoice?>(null)
        private val refreshing = MutableStateFlow(true)
        private val error = MutableStateFlow<Throwable?>(null)

        private val episodeLabel =
            seriesRepository.series(seriesId).map { series ->
                series
                    ?.details
                    ?.episodes
                    ?.firstOrNull { it.id == episodeId }
                    ?.label
            }
        private val access = accountRepository.account.map { it.toAccess() }
        private val selection = combine(pickedKind, choice, ::Pair)
        private val loading = combine(refreshing, error, ::Pair)

        val uiState: StateFlow<TranslationPickerUiState> =
            combine(
                episodeLabel,
                translationRepository.translations(episodeId),
                access,
                selection,
                loading,
            ) { label, translations, access, (picked, choice), (refreshing, error) ->
                TranslationPickerUiState(label, translations, picked, choice, access, refreshing, error)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), TranslationPickerUiState())

        init {
            viewModelScope.launch { choice.value = libraryRepository.choice(seriesId) }
            refresh(force = false)
        }

        fun selectKind(kind: TranslationKind) {
            pickedKind.value = kind
        }

        fun refresh(force: Boolean = true) {
            viewModelScope.launch {
                refreshing.value = true
                error.value = translationRepository.refresh(episodeId, force).exceptionOrNull()
                refreshing.value = false
            }
        }

        private fun Account.toAccess(): PlaybackAccess =
            when {
                this !is Account.User -> PlaybackAccess.NeedsLogin
                !hasSubscription(clock.instant()) -> PlaybackAccess.NeedsSubscription
                else -> PlaybackAccess.Allowed
            }

        @AssistedFactory
        interface Factory {
            fun create(
                @Assisted("seriesId") seriesId: Int,
                @Assisted("episodeId") episodeId: Int,
            ): TranslationPickerViewModel
        }

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
