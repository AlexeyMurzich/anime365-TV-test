package io.github.arenoros.anime365.feature.series.translations

import io.github.arenoros.anime365.core.model.Account
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationChoice
import io.github.arenoros.anime365.core.model.TranslationKind
import io.github.arenoros.anime365.core.testing.FakeAccountRepository
import io.github.arenoros.anime365.core.testing.FakeLibraryRepository
import io.github.arenoros.anime365.core.testing.FakeSeriesRepository
import io.github.arenoros.anime365.core.testing.FakeTranslationRepository
import io.github.arenoros.anime365.core.testing.MainDispatcherRule
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

    private fun TestScope.viewModel(): TranslationPickerViewModel {
        val viewModel =
            TranslationPickerViewModel(
                seriesId = 7,
                episodeId = 101,
                seriesRepository = FakeSeriesRepository(),
                translationRepository = translations,
                libraryRepository = library,
                accountRepository = account,
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )
        backgroundScope.launch { viewModel.uiState.collect {} }
        return viewModel
    }
}
