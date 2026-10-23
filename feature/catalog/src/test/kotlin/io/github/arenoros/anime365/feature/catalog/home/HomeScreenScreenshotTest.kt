package io.github.arenoros.anime365.feature.catalog.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import io.github.arenoros.anime365.feature.catalog.captureScreen
import io.github.arenoros.anime365.feature.catalog.newEpisode
import io.github.arenoros.anime365.feature.catalog.summary
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class HomeScreenScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val content =
        HomeUiState(
            newEpisodes =
                listOf(
                    newEpisode(7, "2026-10-23T09:30:00Z").copy(seriesTitle = "Провожающая в последний путь Фрирен"),
                    newEpisode(12, "2026-10-23T08:05:00Z").copy(seriesTitle = "Чёрный клевер", label = "ONA 5 серия"),
                    newEpisode(3, "2026-10-22T18:40:00Z"),
                ),
            ongoing = (1..4).map { summary(it, title = "Онгоинг с длинным названием $it") },
            seasonTop = (1..3).map { summary(10 + it) },
            isRefreshing = false,
        )

    @Test
    fun content() = capture("home", content)

    @Test
    fun contentLargeFont() = capture("home_font200", content, fontScale = 2f)

    @Test
    fun contentDark() = capture("home_dark", content, darkTheme = true)

    @Test
    fun loading() = capture("home_loading", HomeUiState())

    @Test
    fun offlineWithoutCache() = capture("home_error", HomeUiState(isRefreshing = false, error = IOException()))

    private fun capture(
        name: String,
        state: HomeUiState,
        fontScale: Float = 1f,
        darkTheme: Boolean = false,
    ) = compose.captureScreen(name, fontScale, darkTheme) {
        HomeScreen(
            state = state,
            onRefresh = {},
            onOpenSeries = {},
            onOpenSearch = {},
            onShowAllOngoing = {},
            zone = ZoneId.of("Europe/Moscow"),
            today = LocalDate.of(2026, 10, 23),
        )
    }
}
