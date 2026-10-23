package io.github.arenoros.anime365.feature.series.translations

import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationChoice
import io.github.arenoros.anime365.core.model.TranslationKind
import io.github.arenoros.anime365.core.testing.captureScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class TranslationPickerScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    // 1-я серия Фрирен: Команды бывают пустыми, одна Команда даёт и TV, и BD (docs/api-notes.md).
    private val translations =
        listOf(
            Translation(1, 101, TranslationKind.VoiceRu, "AniLibria", "tv", 1080),
            Translation(2, 101, TranslationKind.VoiceRu, "AniLibria", "bd", 1080),
            Translation(3, 101, TranslationKind.VoiceRu, "Amazing Dubbing", "tv", 1080),
            Translation(4, 101, TranslationKind.VoiceRu, "", "tv", 720),
            Translation(5, 101, TranslationKind.SubRu, "YakuSub Studio (Aero)", "bd", 1080),
            Translation(6, 101, TranslationKind.SubEn, "9volt", "bd", 1080),
            Translation(7, 101, TranslationKind.Raw, "", "tv", 2160),
        )
    private val loaded =
        TranslationPickerUiState(
            episodeLabel = "1 серия",
            translations = translations,
            choice = TranslationChoice(TranslationKind.VoiceRu, "AniLibria"),
            isRefreshing = false,
        )

    @Test
    fun picker() = capture("translations", loaded)

    @Test
    fun pickerLargeFont() = capture("translations_font200", loaded, fontScale = 2f)

    @Test
    fun guestSeesListWithLoginBanner() =
        capture("translations_guest", loaded.copy(access = PlaybackAccess.NeedsLogin, choice = null))

    @Test
    fun loading() = capture("translations_loading", TranslationPickerUiState(episodeLabel = "1 серия"))

    private fun capture(
        name: String,
        state: TranslationPickerUiState,
        fontScale: Float = 1f,
    ) = compose.captureScreen(name, fontScale) {
        Surface { TranslationPickerSheet(state, onSelectKind = {}, onRetry = {}, onSelect = {}) }
    }
}
