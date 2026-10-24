package io.github.arenoros.anime365.feature.series

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.model.Description
import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.EpisodeProgress
import io.github.arenoros.anime365.core.model.ExternalLink
import io.github.arenoros.anime365.core.model.Genre
import io.github.arenoros.anime365.core.model.LastWatched
import io.github.arenoros.anime365.core.model.LibraryStatus
import io.github.arenoros.anime365.core.model.ReleaseStatus
import io.github.arenoros.anime365.core.model.Series
import io.github.arenoros.anime365.core.model.SeriesDetails
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.core.testing.ScreenshotOptions
import io.github.arenoros.anime365.core.testing.captureScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class SeriesScreenScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val summary =
        SeriesSummary(
            id = 30414,
            title = "Провожающая в последний путь Фрирен",
            originalTitle = "Sousou no Frieren",
            posterUrl = null,
            kind = "ТВ сериал",
            episodeCount = 28,
            year = 2023,
            season = "Осень 2023",
            score = 9.24,
            status = ReleaseStatus.Ongoing,
        )

    private val details =
        SeriesDetails(
            type = "tv",
            largePosterUrl = null,
            siteUrl = "https://smotret-anime.co/catalog/sousou-no-frieren-30414",
            genres = listOf(Genre(2, "Приключения"), Genre(10, "Фэнтези"), Genre(27, "Сёнен"), Genre(8, "Драма")),
            descriptions =
                listOf(
                    Description("shikimori.me", "Одержав победу над Королём демонов, отряд героя вернулся домой."),
                    Description("world-art.ru", "Второе описание."),
                ),
            links =
                listOf(
                    ExternalLink("MyAnimeList", "https://myanimelist.net/anime/52991"),
                    ExternalLink("Шикимори", "https://shikimori.io"),
                ),
            episodes =
                listOf(Episode(1, 1.0, "Трейлер", "preview")) +
                    (1..10).map { Episode(100 + it, it.toDouble(), "$it серия", "tv") },
        )

    private val loaded = SeriesUiState(Series(summary, details), isRefreshing = false)

    @Test
    fun description() = capture("series_description", loaded)

    @Test
    fun descriptionLargeFont() = capture("series_description_font200", loaded, fontScale = 2f)

    @Test
    fun episodes() {
        compose.setContent { TestScreen(loaded) }
        // Семантическое действие, а не касание: волна от нажатия делала бы снимок зависимым от кадра.
        compose.onNodeWithText("СЕРИИ").performSemanticsAction(SemanticsActions.OnClick)
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("src/test/screenshots/series_episodes.png", ScreenshotOptions)
    }

    @Test
    fun episodesOfWatchingSeriesWithNewOnes() {
        val watching =
            loaded.copy(
                status = LibraryStatus.Watching,
                progress = (101..108).associateWith { EpisodeProgress(it, 1000, 1000, watched = true) },
                lastWatched = LastWatched(108, "8 серия", 1, EpisodeProgress(108, 1000, 1000, watched = true)),
                newEpisodes = setOf(109, 110),
            )
        compose.setContent { TestScreen(watching) }
        compose.onNodeWithText("СЕРИИ").performSemanticsAction(SemanticsActions.OnClick)
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("src/test/screenshots/series_episodes_new.png", ScreenshotOptions)
    }

    @Test
    fun statusSheet() =
        compose.captureScreen("series_status") {
            Surface { StatusOptions(LibraryStatus.Watching, onSelect = {}) }
        }

    @Test
    fun episodeActions() =
        compose.captureScreen("series_episode_actions") {
            Surface {
                EpisodeActions(
                    Episode(105, 5.0, "5 серия", "tv"),
                    watched = false,
                    hasFollowing = true,
                    onDownload = {},
                    onSetWatched = {},
                )
            }
        }

    @Test
    fun summaryOnlyWhileLoading() = capture("series_loading", SeriesUiState(Series(summary, details = null)))

    @Test
    fun offlineWithoutCache() = capture("series_error", SeriesUiState(isRefreshing = false, error = IOException()))

    private fun capture(
        name: String,
        state: SeriesUiState,
        fontScale: Float = 1f,
    ) = compose.captureScreen(name, fontScale) { TestScreen(state) }

    @Composable
    private fun TestScreen(state: SeriesUiState) =
        KadrTheme {
            SeriesScreen(
                state = state,
                onRetry = {},
                onOpenGenre = {},
                onOpenEpisode = {},
                onPlay = { _, _ -> },
                onSetStatus = {},
                onSetWatched = { _, _ -> },
                onBack = {},
            )
        }
}
