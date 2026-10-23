package io.github.arenoros.anime365.feature.library

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import io.github.arenoros.anime365.core.model.LibraryEntry
import io.github.arenoros.anime365.core.model.LibraryStatus
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

    private fun capture(
        name: String,
        state: LibraryUiState,
        fontScale: Float = 1f,
    ) = compose.captureScreen(name, fontScale) {
        LibraryScreen(state, onOpenSeries = {}, onContinue = {}, onDismissHint = {})
    }
}
