package io.github.arenoros.anime365.feature.library

import io.github.arenoros.anime365.core.data.repository.RememberedTranslation
import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.EpisodeProgress
import io.github.arenoros.anime365.core.model.LastWatched
import io.github.arenoros.anime365.core.model.LibraryEntry
import io.github.arenoros.anime365.core.model.LibraryStatus
import io.github.arenoros.anime365.core.model.ReleaseStatus
import io.github.arenoros.anime365.core.model.Series
import io.github.arenoros.anime365.core.model.SeriesDetails
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationChoice
import io.github.arenoros.anime365.core.model.TranslationKind
import io.github.arenoros.anime365.core.testing.FakeLibraryRepository
import io.github.arenoros.anime365.core.testing.FakeSeriesRepository
import io.github.arenoros.anime365.core.testing.FakeTranslationRepository
import io.github.arenoros.anime365.core.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class LibraryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val library = FakeLibraryRepository()
    private val series = FakeSeriesRepository()
    private val translations = FakeTranslationRepository()

    private val first = Episode(101, 1.0, "1 серия", "tv")
    private val second = Episode(102, 2.0, "2 серия", "tv")
    private val frieren =
        Series(
            SeriesSummary(7, "Фрирен", null, null, null, 28, 2023, null, 9.2, ReleaseStatus.Ongoing),
            SeriesDetails("tv", null, null, emptyList(), emptyList(), emptyList(), listOf(first, second)),
        )

    @Test
    fun entriesAreSplitByStatus() =
        runTest(mainDispatcherRule.dispatcher) {
            library.entries.value =
                listOf(
                    LibraryEntry(7, "Фрирен", null, LibraryStatus.Watching, 1, 28, 2),
                    LibraryEntry(8, "Атака титанов", null, LibraryStatus.Planned, 0, 25, 0),
                )
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }

            assertEquals(
                listOf(7),
                viewModel.uiState.value
                    .of(LibraryStatus.Watching)
                    .map { it.seriesId },
            )
            assertEquals(
                listOf(8),
                viewModel.uiState.value
                    .of(LibraryStatus.Planned)
                    .map { it.seriesId },
            )
        }

    @Test
    fun hintIsShownUntilDismissed() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = viewModel()
            backgroundScope.launch { viewModel.uiState.collect {} }
            assertFalse(viewModel.uiState.value.hintShown)

            viewModel.dismissHint()

            assertEquals(true, viewModel.uiState.value.hintShown)
        }

    @Test
    fun unfinishedEpisodeResumesWithoutLoadingSeries() =
        runTest(mainDispatcherRule.dispatcher) {
            library.lastWatched.value =
                LastWatched(101, "1 серия", 55, EpisodeProgress(101, 300, 1000, watched = false))

            assertEquals(LibraryEvent.Play(7, 101, 55), continueWatching())
            assertEquals(emptyList<Any>(), series.refreshCalls)
        }

    @Test
    fun watchedEpisodeContinuesWithNextOfRememberedTeam() =
        runTest(mainDispatcherRule.dispatcher) {
            library.lastWatched.value =
                LastWatched(101, "1 серия", 55, EpisodeProgress(101, 1000, 1000, watched = true))
            library.choice = TranslationChoice(TranslationKind.VoiceRu, "AniLibria")
            series.series.value = frieren
            translations.translations.value =
                mapOf(102 to listOf(Translation(9, 102, TranslationKind.VoiceRu, "AniLibria", "tv", 1080)))

            assertEquals(LibraryEvent.Play(7, 102, 9), continueWatching())
        }

    @Test
    fun neverWatchedStartsWithTranslationChoice() =
        runTest(mainDispatcherRule.dispatcher) {
            series.series.value = frieren

            assertEquals(LibraryEvent.ChooseTranslation(7, 101), continueWatching())
        }

    @Test
    fun allWatchedOpensSeries() =
        runTest(mainDispatcherRule.dispatcher) {
            library.lastWatched.value =
                LastWatched(102, "2 серия", 55, EpisodeProgress(102, 1000, 1000, watched = true))
            series.series.value = frieren

            assertEquals(LibraryEvent.OpenSeries(7), continueWatching())
        }

    private fun viewModel() = LibraryViewModel(library, series, RememberedTranslation(library, translations))

    private fun TestScope.continueWatching(): LibraryEvent {
        val viewModel = viewModel()
        val events = mutableListOf<LibraryEvent>()
        backgroundScope.launch { viewModel.events.collect { events += it } }
        viewModel.continueWatching(7)
        return events.single()
    }
}
