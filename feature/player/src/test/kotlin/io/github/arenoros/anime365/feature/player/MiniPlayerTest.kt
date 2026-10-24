package io.github.arenoros.anime365.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.core.designsystem.tokens.KadrSpacing
import io.github.arenoros.anime365.core.testing.captureScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class MiniPlayerTest {
    @get:Rule
    val compose = createComposeRule()

    private var expanded = 0
    private var closed = 0

    @Test
    fun tapExpandsAndCrossCloses() {
        setContent()

        compose.onNodeWithContentDescription(WINDOW).performClick()
        compose.onNodeWithContentDescription("Закрыть Плеер").performClick()

        assertEquals(1, expanded)
        assertEquals(1, closed)
    }

    @Test
    fun releasedWindowSnapsToNearestCorner() {
        setContent()
        val window = compose.onNodeWithContentDescription(WINDOW)
        val before = window.getBoundsInRoot()

        // Чуть дальше середины влево — дальше окно доезжает до угла само.
        val distance = (before.left - KadrSpacing.Space3) * 0.6f
        window.performTouchInput { swipe(center, center - Offset(distance.toPx(), 0f)) }
        compose.waitForIdle()

        val after = window.getBoundsInRoot()
        assertEquals(KadrSpacing.Space3.value, after.left.value, 0.5f)
        assertEquals(before.top.value, after.top.value, 0.5f)
        assertEquals(0, expanded)
    }

    @Test
    fun screenshot() =
        compose.captureScreen("player_mini") {
            Box(Modifier.fillMaxSize().background(KadrTheme.palette.background)) {
                MiniPlayer(player = null, onExpand = {}, onClose = {})
            }
        }

    private fun setContent() =
        compose.setContent {
            KadrTheme { MiniPlayer(player = null, onExpand = { expanded++ }, onClose = { closed++ }) }
        }

    private companion object {
        const val WINDOW = "Плеер в окне"
    }
}
