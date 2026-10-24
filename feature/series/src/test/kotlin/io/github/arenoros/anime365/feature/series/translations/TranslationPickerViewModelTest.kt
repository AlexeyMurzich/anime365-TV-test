package io.github.arenoros.anime365.feature.series.translations

import io.github.arenoros.anime365.core.data.repository.SubscriptionRequiredException
import io.github.arenoros.anime365.core.model.Account
import io.github.arenoros.anime365.core.model.DownloadState
import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.ReleaseStatus
import io.github.arenoros.anime365.core.model.Series
import io.github.arenoros.anime365.core.model.SeriesDetails
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.core.model.StreamQuality
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationChoice
import io.github.arenoros.anime365.core.model.TranslationKind
import io.github.arenoros.anime365.core.model.VideoStream
import io.github.arenoros.anime365.core.testing.FakeAccountRepository
import io.github.arenoros.anime365.core.testing.FakeEpisodeDownloads
import io.github.arenoros.anime365.core.testing.FakeLibraryRepository
import io.github.arenoros.anime365.core.testing.FakeSeriesRepository
import io.github.arenoros.anime365.core.testing.FakeTranslationRepository
import io.github.arenoros.anime365.core.testing.MainDispatcherRule
import io.github.arenoros.anime365.feature.series.navigation.PickerPurpose
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class TranslationPickerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val now = Instant.parse("2026-10-23T12:00:00Z")
    private val translations = FakeTranslationRepository()
    private val library = FakeLibraryRepository()
    private val account = FakeAccountRepository()
    private val downloads = FakeEpisodeDownloads()

    private val episodes =
        listOf(
            Episode(100, 1.0, "Трейлер", "preview"),
            Episode(101, 1.0, "1 серия", "tv"),
            Episode(102, 2.0, "2 серия", "tv"),
            Episode(103, 3.0, "3 серия", "tv"),
            Episode(104, 4.0, "4 серия", "tv"),
        )
    private val series =
        FakeSeriesRepository().apply {
            series.value =
                Series(
                    SeriesSummary(7, "Фрирен", null, null, "ТВ сериал", 28, 2023, null, 9.2, ReleaseStatus.Ongoing),
                    SeriesDetails("tv", null, null, emptyList(), emptyList(), emptyList(), episodes),
                )
        }

    private val sub = Translation(1, 101, TranslationKind.SubRu, "YakuSub", "bd", 1080)
    private val voiceTv = Translation(2, 101, TranslationKind.VoiceRu, "AniLibria", "tv", 720)
    private val voiceBd = Translation(3, 101, TranslationKind.VoiceRu, "AniLibria", "bd", 1080)
    private val raw = Translation(4, 101, TranslationKind.Raw, "", null, 1080)

    @Test
    fun kindsFollowPickerOrderAndRussianVoiceIsDefault() =
        runTest(mainDispatcherRule.dispatcher) {
            translations.translations.value = mapOf(101 to listOf(raw, sub, voiceTv))

            val state = viewModel().uiState.value

            assertEquals(listOf(TranslationKind.VoiceRu, TranslationKind.SubRu, TranslationKind.Raw), state.kinds)
            assertEquals(TranslationKind.VoiceRu, state.selectedKind)
            assertEquals(listOf(voiceTv), state.shown)
        }

    @Test
    fun rememberedChoiceSelectsKindAndMarksTeam() =
        runTest(mainDispatcherRule.dispatcher) {
            translations.translations.value = mapOf(101 to listOf(sub, voiceTv, voiceBd))
            library.choice = TranslationChoice(TranslationKind.SubRu, "YakuSub")

            val state = viewModel().uiState.value

            assertEquals(TranslationKind.SubRu, state.selectedKind)
            assertTrue(state.isRemembered(sub))
            assertFalse(state.isRemembered(voiceTv))
        }

    @Test
    fun pickedKindWins() =
        runTest(mainDispatcherRule.dispatcher) {
            translations.translations.value = mapOf(101 to listOf(sub, voiceTv, voiceBd))
            val viewModel = viewModel()

            viewModel.selectKind(TranslationKind.SubRu)

            assertEquals(listOf(sub), viewModel.uiState.value.shown)
        }

    @Test
    fun accessDependsOnLoginAndSubscription() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = viewModel()
            assertEquals(PlaybackAccess.NeedsLogin, viewModel.uiState.value.access)

            account.account.value = Account.User("Фрирен", subscriptionUntil = now.minusSeconds(1))
            assertEquals(PlaybackAccess.NeedsSubscription, viewModel.uiState.value.access)

            account.account.value = Account.User("Фрирен", subscriptionUntil = now.plusSeconds(1))
            assertEquals(PlaybackAccess.Allowed, viewModel.uiState.value.access)
        }

    @Test
    fun downloadEnqueuesEpisodeAndClosesSheet() =
        runTest(mainDispatcherRule.dispatcher) {
            translations.translations.value = mapOf(101 to listOf(voiceTv))
            translations.stream = { Result.success(streamOf(it)) }
            val viewModel = viewModel(PickerPurpose.DownloadEpisode)
            val events = mutableListOf<PickerEvent>()
            backgroundScope.launch { viewModel.events.collect { events += it } }

            viewModel.download(voiceTv)

            assertEquals(listOf(PickerEvent.Enqueued), events)
            assertEquals(listOf(voiceTv), downloads.downloads.value.map { it.translation })
            assertEquals(DownloadState.Queued, viewModel.uiState.value.downloads[voiceTv.id])
        }

    @Test
    fun downloadFollowingTakesSameTeamAndSkipsMissingAndDownloaded() =
        runTest(mainDispatcherRule.dispatcher) {
            val second = Translation(12, 102, TranslationKind.VoiceRu, "AniLibria", "tv", 720)
            val thirdOtherTeam = Translation(13, 103, TranslationKind.VoiceRu, "SHIZA Project", "tv", 720)
            val fourth = Translation(14, 104, TranslationKind.VoiceRu, "AniLibria", "tv", 720)
            translations.translations.value =
                mapOf(
                    101 to listOf(voiceTv),
                    102 to listOf(second),
                    103 to listOf(thirdOtherTeam),
                    104 to listOf(fourth),
                )
            translations.stream = { Result.success(streamOf(it)) }
            downloads.download(series.series.value!!.summary, episodes[4], fourth, streamOf(fourth.id))
            translations.streamRequests.clear()

            viewModel(PickerPurpose.DownloadFollowing).download(voiceTv)

            assertEquals(listOf(14, 2, 12), downloads.downloads.value.map { it.translation.id })
            assertEquals(listOf(2, 12), translations.streamRequests)
        }

    @Test
    fun downloadWithoutSubscriptionLeadsToSubscriptionScreen() =
        runTest(mainDispatcherRule.dispatcher) {
            translations.translations.value = mapOf(101 to listOf(voiceTv))
            translations.stream = { Result.failure(SubscriptionRequiredException()) }
            val viewModel = viewModel(PickerPurpose.DownloadEpisode)
            val events = mutableListOf<PickerEvent>()
            backgroundScope.launch { viewModel.events.collect { events += it } }

            viewModel.download(voiceTv)

            assertEquals(listOf(PickerEvent.NeedsSubscription), events)
            assertTrue(downloads.downloads.value.isEmpty())
        }

    private fun streamOf(translationId: Int) =
        VideoStream(
            listOf(StreamQuality(720, "https://cdn/$translationId")),
            subtitlesUrl = null,
            downloads =
                listOf(
                    StreamQuality(720, "https://site/translations/mp4/$translationId?download=1&height=720"),
                ),
        )

    private fun TestScope.viewModel(purpose: PickerPurpose = PickerPurpose.Watch): TranslationPickerViewModel {
        val viewModel =
            TranslationPickerViewModel(
                seriesId = 7,
                episodeId = 101,
                purpose = purpose,
                seriesRepository = series,
                translationRepository = translations,
                libraryRepository = library,
                accountRepository = account,
                downloads = downloads,
                applicationScope = this,
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )
        backgroundScope.launch { viewModel.uiState.collect {} }
        return viewModel
    }
}
