package io.github.arenoros.anime365.feature.library

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import io.github.arenoros.anime365.core.model.DownloadState
import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.EpisodeDownload
import io.github.arenoros.anime365.core.model.LibraryEntry
import io.github.arenoros.anime365.core.model.LibraryStatus
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationKind
import io.github.arenoros.anime365.core.testing.captureScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// Вкладки Статусов по порядку, Скачанное — после них.
private val DOWNLOADS_TAB = LibraryStatus.entries.size
private const val MEGABYTES_529 = 529L * 1024 * 1024

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class LibraryScreenScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val watching =
        LibraryUiState(
            entries =
                listOf(
                    LibraryEntry(30414, "Провожающая в последний путь Фрирен", null, LibraryStatus.Watching, 12, 28, 2),
                    LibraryEntry(1, "Магическая битва", null, LibraryStatus.Watching, 3, 23, 0),
                    // Число Эпизодов неизвестно: на сайте 0.
                    LibraryEntry(2, "Ван-Пис", null, LibraryStatus.Watching, 1071, null, 0),
                ),
            hintShown = true,
        )

    @Test
    fun watching() = capture("library_watching", watching)

    @Test
    fun watchingLargeFont() = capture("library_watching_font200", watching, fontScale = 2f)

    @Test
    fun firstOpenShowsLocalOnlyHint() = capture("library_hint", watching.copy(hintShown = false))

    @Test
    fun emptyTab() = capture("library_empty", LibraryUiState(entries = emptyList()))

    @Test
    fun downloads() = capture("library_downloads", watching.copy(downloads = downloadStates()), tab = DOWNLOADS_TAB)

    @Test
    fun downloadsLargeFont() =
        capture("library_downloads_font200", watching.copy(downloads = downloadStates()), 2f, DOWNLOADS_TAB)

    @Test
    fun downloadsEmpty() = capture("library_downloads_empty", watching, tab = DOWNLOADS_TAB)

    private fun downloadStates(): List<EpisodeDownload> {
        fun download(
            seriesId: Int,
            title: String,
            number: Int,
            state: DownloadState,
            bytes: Long,
        ) = EpisodeDownload(
            seriesId,
            title,
            null,
            Episode(seriesId * 100 + number, number.toDouble(), "$number серия", "tv"),
            Translation(
                seriesId * 100 + number,
                seriesId * 100 + number,
                TranslationKind.VoiceRu,
                "AniLibria",
                "tv",
                1080,
            ),
            720,
            "https://site/$number",
            null,
            state,
            bytes,
            contentLength = if (state == DownloadState.Queued) null else MEGABYTES_529,
        )
        return listOf(
            download(30414, "Провожающая в последний путь Фрирен", 5, DownloadState.Completed, MEGABYTES_529),
            download(
                30414,
                "Провожающая в последний путь Фрирен",
                6,
                DownloadState.Downloading,
                MEGABYTES_529 * 43 / 100,
            ),
            download(30414, "Провожающая в последний путь Фрирен", 7, DownloadState.WaitingForWifi, 0),
            download(9999, "Ковбой Бибоп", 1, DownloadState.Queued, 0),
            download(9999, "Ковбой Бибоп", 2, DownloadState.Failed, 0),
        )
    }

    private fun capture(
        name: String,
        state: LibraryUiState,
        fontScale: Float = 1f,
        tab: Int = 0,
    ) = compose.captureScreen(name, fontScale) {
        LibraryScreen(state, onOpenSeries = {}, onContinue = {}, onDismissHint = {}, initialTab = tab)
    }
}
