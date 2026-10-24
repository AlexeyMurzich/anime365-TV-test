package io.github.arenoros.anime365.feature.series.translations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.arenoros.anime365.core.data.di.ApplicationScope
import io.github.arenoros.anime365.core.data.repository.AccountRepository
import io.github.arenoros.anime365.core.data.repository.LibraryRepository
import io.github.arenoros.anime365.core.data.repository.NotLoggedInException
import io.github.arenoros.anime365.core.data.repository.SeriesRepository
import io.github.arenoros.anime365.core.data.repository.SubscriptionRequiredException
import io.github.arenoros.anime365.core.data.repository.TranslationRepository
import io.github.arenoros.anime365.core.model.Account
import io.github.arenoros.anime365.core.model.DownloadState
import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.Series
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationChoice
import io.github.arenoros.anime365.core.model.TranslationKind
import io.github.arenoros.anime365.core.model.ofTeam
import io.github.arenoros.anime365.core.player.EpisodeDownloads
import io.github.arenoros.anime365.feature.series.navigation.PickerPurpose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
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
    val purpose: PickerPurpose = PickerPurpose.Watch,
    /** Скачивания Переводов этого Эпизода: Скачанное играет без сети. */
    val downloads: Map<Int, DownloadState> = emptyMap(),
    /** Ставим в очередь: Видеопоток ещё не получен. */
    val isEnqueuing: Boolean = false,
    /** Поставить в очередь не вышло — сеть или сервер. */
    val downloadError: Throwable? = null,
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

internal sealed interface PickerEvent {
    /** Поставлено в очередь — лист закрывается. */
    data object Enqueued : PickerEvent

    data object NeedsLogin : PickerEvent

    data object NeedsSubscription : PickerEvent
}

@HiltViewModel(assistedFactory = TranslationPickerViewModel.Factory::class)
internal class TranslationPickerViewModel
    @AssistedInject
    constructor(
        @Assisted("seriesId") private val seriesId: Int,
        @Assisted("episodeId") private val episodeId: Int,
        @Assisted private val purpose: PickerPurpose,
        private val seriesRepository: SeriesRepository,
        private val translationRepository: TranslationRepository,
        private val libraryRepository: LibraryRepository,
        accountRepository: AccountRepository,
        private val downloads: EpisodeDownloads,
        @param:ApplicationScope private val applicationScope: CoroutineScope,
        private val clock: Clock,
    ) : ViewModel() {
        private val pickedKind = MutableStateFlow<TranslationKind?>(null)
        private val choice = MutableStateFlow<TranslationChoice?>(null)
        private val refreshing = MutableStateFlow(true)
        private val error = MutableStateFlow<Throwable?>(null)
        private val enqueuing = MutableStateFlow(false)
        private val downloadError = MutableStateFlow<Throwable?>(null)

        private val eventChannel = Channel<PickerEvent>(Channel.BUFFERED)
        val events: Flow<PickerEvent> = eventChannel.receiveAsFlow()

        private val episodeLabel =
            seriesRepository.series(seriesId).map { series -> series?.episode()?.label }
        private val access = accountRepository.account.map { it.toAccess() }
        private val selection = combine(pickedKind, choice, ::Pair)
        private val loading = combine(refreshing, error, ::Pair)
        private val downloadStates =
            combine(downloads.downloads, enqueuing, downloadError) { list, enqueuing, error ->
                Triple(
                    list.filter { it.episode.id == episodeId }.associate { it.translation.id to it.state },
                    enqueuing,
                    error,
                )
            }
        private val content =
            combine(episodeLabel, translationRepository.translations(episodeId), access, ::Triple)

        val uiState: StateFlow<TranslationPickerUiState> =
            combine(
                content,
                selection,
                loading,
                downloadStates,
            ) { (label, translations, access), (picked, choice), (refreshing, error), (states, enqueuing, failed) ->
                TranslationPickerUiState(
                    label,
                    translations,
                    picked,
                    choice,
                    access,
                    refreshing,
                    error,
                    purpose,
                    states,
                    enqueuing,
                    failed,
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                TranslationPickerUiState(purpose = purpose),
            )

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

        /**
         * Сначала — эта серия: её ошибку видно в листе. Следующие той же Командой ставятся уже после
         * закрытия листа; серии без этой Команды и уже скачанные пропускаются.
         */
        fun download(translation: Translation) {
            if (enqueuing.value) return
            viewModelScope.launch {
                enqueuing.value = true
                downloadError.value = null
                val series = seriesRepository.series(seriesId).first()
                val episode = series?.episode()
                val result =
                    if (series == null || episode == null) {
                        Result.failure(IllegalStateException("Episode $episodeId of $seriesId is gone"))
                    } else {
                        enqueue(series, episode, translation)
                    }
                enqueuing.value = false
                result
                    .onSuccess {
                        if (purpose == PickerPurpose.DownloadFollowing && series != null && episode != null) {
                            applicationScope.launch { enqueueFollowing(series, episode, translation) }
                        }
                        eventChannel.send(PickerEvent.Enqueued)
                    }.onFailure { failure ->
                        when (failure) {
                            is NotLoggedInException -> eventChannel.send(PickerEvent.NeedsLogin)
                            is SubscriptionRequiredException -> eventChannel.send(PickerEvent.NeedsSubscription)
                            else -> downloadError.value = failure
                        }
                    }
            }
        }

        private suspend fun enqueue(
            series: Series,
            episode: Episode,
            translation: Translation,
        ): Result<Unit> =
            translationRepository.stream(translation.id).map { stream ->
                downloads.download(series.summary, episode, translation, stream)
            }

        private suspend fun enqueueFollowing(
            series: Series,
            from: Episode,
            translation: Translation,
        ) {
            val episodes = series.details?.episodes.orEmpty()
            val known =
                downloads.downloads
                    .first()
                    .map { it.translation.id }
                    .toSet()
            val team = TranslationChoice(translation.kind, translation.team)
            episodes
                .drop(episodes.indexOf(from) + 1)
                // Трейлеры и спешлы — не «следующие серии».
                .filter { it.type == from.type }
                .forEach { episode ->
                    val match =
                        translationRepository.load(episode.id).getOrNull()?.ofTeam(team, translation.source)
                    if (match != null && match.id !in known) enqueue(series, episode, match)
                }
        }

        private fun Series.episode(): Episode? = details?.episodes?.firstOrNull { it.id == episodeId }

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
                purpose: PickerPurpose,
            ): TranslationPickerViewModel
        }

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
