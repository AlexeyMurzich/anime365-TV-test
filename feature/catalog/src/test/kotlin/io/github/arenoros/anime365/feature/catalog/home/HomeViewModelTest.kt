package io.github.arenoros.anime365.feature.catalog.home

import io.github.arenoros.anime365.core.data.repository.HomeRepository
import io.github.arenoros.anime365.core.data.repository.RememberedTranslation
import io.github.arenoros.anime365.core.model.ContinueWatching
import io.github.arenoros.anime365.core.model.EpisodeProgress
import io.github.arenoros.anime365.core.model.FeedEpisode
import io.github.arenoros.anime365.core.model.LastWatched
import io.github.arenoros.anime365.core.model.LibraryEntry
import io.github.arenoros.anime365.core.model.LibraryStatus
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationChoice
import io.github.arenoros.anime365.core.model.TranslationKind
import io.github.arenoros.anime365.core.testing.FakeLibraryRepository
import io.github.arenoros.anime365.core.testing.FakeTranslationRepository
import io.github.arenoros.anime365.core.testing.MainDispatcherRule
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
    private val library = FakeLibraryRepository()
    private val translations = FakeTranslationRepository()

    @Test
    fun opensWithRefreshThatRespectsCacheAge() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = HomeViewModel(repository, library, RememberedTranslation(library, translations))
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
            val viewModel = HomeViewModel(repository, library, RememberedTranslation(library, translations))
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
            val viewModel = HomeViewModel(repository, library, RememberedTranslation(library, translations))
            backgroundScope.launch { viewModel.uiState.collect {} }

            repository.result = CompletableDeferred(Result.success(Unit))
            viewModel.refresh()

            assertEquals(listOf(false, true), repository.refreshCalls)
            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun continueWatchingIsShownEvenWithoutSiteData() =
        runTest(mainDispatcherRule.dispatcher) {
            val card =
                ContinueWatching(
                    7,
                    "Фрирен",
                    null,
                    LastWatched(101, "1 серия", 55, EpisodeProgress(101, 300, 1000, watched = false)),
                )
            library.continueWatching.value = listOf(card)
            val viewModel = HomeViewModel(repository, library, RememberedTranslation(library, translations))
            backgroundScope.launch { viewModel.uiState.collect {} }

            repository.result.complete(Result.failure(IOException("offline")))

            assertEquals(listOf(card), viewModel.uiState.value.continueWatching)
            assertFalse("Продолжить просмотр — из Библиотеки, экран не пустой", viewModel.uiState.value.isEmpty)
        }

    @Test
    fun seriesWatchingAreMarked() =
        runTest(mainDispatcherRule.dispatcher) {
            library.entries.value =
                listOf(
                    LibraryEntry(7, "Фрирен", null, LibraryStatus.Watching, 1, 28, 0),
                    LibraryEntry(8, "Атака титанов", null, LibraryStatus.Planned, 0, 25, 0),
                )
            val viewModel = HomeViewModel(repository, library, RememberedTranslation(library, translations))
            backgroundScope.launch { viewModel.uiState.collect {} }

            assertEquals(setOf(7), viewModel.uiState.value.watching)
        }

    @Test
    fun newEpisodeOpensPlayerWithRememberedTeam() =
        runTest(mainDispatcherRule.dispatcher) {
            library.choice = TranslationChoice(TranslationKind.VoiceRu, "AniLibria")
            translations.translations.value =
                mapOf(
                    108 to
                        listOf(
                            Translation(1, 108, TranslationKind.VoiceRu, "SHIZA Project", "tv", 1080),
                            Translation(2, 108, TranslationKind.VoiceRu, "AniLibria", "tv", 1080),
                        ),
                )
            val viewModel = HomeViewModel(repository, library, RememberedTranslation(library, translations))
            val events = mutableListOf<HomeEvent>()
            backgroundScope.launch { viewModel.events.collect { events += it } }

            viewModel.openEpisode(seriesId = 7, episodeId = 108)
            viewModel.openEpisode(seriesId = 7, episodeId = 109)

            assertEquals(listOf(HomeEvent.Play(7, 108, 2), HomeEvent.ChooseTranslation(7, 109)), events)
        }

    private class FakeHomeRepository : HomeRepository {
        override val feed = MutableStateFlow(emptyList<FeedEpisode>())
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
