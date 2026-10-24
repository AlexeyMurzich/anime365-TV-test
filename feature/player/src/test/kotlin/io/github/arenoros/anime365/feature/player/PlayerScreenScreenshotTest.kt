package io.github.arenoros.anime365.feature.player

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.EpisodeProgress
import io.github.arenoros.anime365.core.model.ReleaseStatus
import io.github.arenoros.anime365.core.model.Series
import io.github.arenoros.anime365.core.model.SeriesDetails
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationKind
import io.github.arenoros.anime365.core.testing.captureScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Видео в Robolectric не нарисовать: снимаем управление, состояния и портретную часть на чёрном. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class PlayerScreenScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val episodes = (1..10).map { Episode(100 + it, it.toDouble(), "$it серия", "tv") }
    private val playing =
        PlayerUiState(
            series =
                Series(
                    SeriesSummary(
                        30414,
                        "Провожающая в последний путь Фрирен",
                        null,
                        null,
                        "ТВ сериал",
                        28,
                        2023,
                        null,
                        9.2,
                        ReleaseStatus.Ongoing,
                    ),
                    SeriesDetails("tv", null, null, emptyList(), emptyList(), emptyList(), episodes),
                ),
            episode = episodes[6],
            translation = Translation(1, 107, TranslationKind.VoiceRu, "AniLibria", "tv", 1080),
            status = PlayerStatus.Playing,
            qualities = listOf(1080, 720, 360),
            quality = 1080,
            progress =
                mapOf(
                    101 to EpisodeProgress(101, 100, 100, watched = true),
                    102 to EpisodeProgress(102, 100, 100, watched = true),
                    107 to EpisodeProgress(107, 30, 100, watched = false),
                ),
        )

    @Test
    fun portrait() = capture("player", playing)

    @Test
    fun portraitDark() = capture("player_dark", playing, darkTheme = true)

    @Test
    fun portraitLargeFont() = capture("player_font200", playing, fontScale = 2f)

    @Test
    @Config(qualifiers = "+land")
    fun landscape() = capture("player_landscape", playing)

    @Test
    fun nextEpisodeCountdown() = capture("player_countdown", playing.copy(nextCountdown = 5))

    @Test
    fun subscriptionRequired() = capture("player_subscription", playing.copy(status = PlayerStatus.NeedsSubscription))

    private fun capture(
        name: String,
        state: PlayerUiState,
        fontScale: Float = 1f,
        darkTheme: Boolean = false,
    ) = compose.captureScreen(name, fontScale, darkTheme) {
        PlayerScreen(
            state = state,
            player = null,
            snackbars = SnackbarHostState(),
            collapse = null,
            actions = PlayerActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}),
        )
    }
}
