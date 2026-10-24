package io.github.arenoros.anime365.feature.player

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class PlayerCollapseTest {
    @get:Rule
    val compose = createComposeRule()

    private val video = Rect(0f, 100f, 1080f, 708f)
    private val window = Rect(600f, 1800f, 1050f, 2053f)
    private var collapsed = 0

    @Test
    fun dragMovesVideoTowardsWindowAndEndsOnIt() {
        val collapse = PlayerCollapse({ window }) { collapsed++ }
        collapse.videoBounds = video

        collapse.dragBy((window.top - video.top) / 2)

        assertEquals(0.5f, collapse.fraction, 0.001f)
        assertEquals(window, collapse.videoAt(1f))
        assertEquals(video, collapse.videoAt(0f))
    }

    @Test
    fun releaseDecidesByDistanceOrFling() {
        assertTrue(shouldCollapse(fraction = 0.5f, velocity = 0f, flingVelocity = 1000f))
        assertFalse(shouldCollapse(fraction = 0.1f, velocity = 0f, flingVelocity = 1000f))
        assertTrue(shouldCollapse(fraction = 0.1f, velocity = 2000f, flingVelocity = 1000f))
        assertFalse(shouldCollapse(fraction = 0.9f, velocity = -2000f, flingVelocity = 1000f))
    }

    @Test
    fun swipeDownOnVideoCollapsesPlayer() {
        val collapse = setContent()

        compose.onRoot().performTouchInput { swipeDown(startY = 200f, endY = 1500f, durationMillis = 1000) }
        compose.waitForIdle()

        assertEquals(1, collapsed)
        assertEquals(1f, collapse.fraction)
    }

    @Test
    fun shortSlowSwipeReturnsPlayer() {
        val collapse = setContent()

        compose.onRoot().performTouchInput { swipeDown(startY = 200f, endY = 400f, durationMillis = 1000) }
        compose.waitForIdle()

        assertEquals(0, collapsed)
        assertEquals(0f, collapse.fraction)
    }

    private fun setContent(): PlayerCollapse {
        val collapse = PlayerCollapse({ window }) { collapsed++ }
        compose.setContent {
            KadrTheme {
                PlayerScreen(
                    state = PlayerUiState(status = PlayerStatus.Playing),
                    player = null,
                    snackbars = SnackbarHostState(),
                    collapse = collapse,
                    actions = PlayerActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}),
                )
            }
        }
        return collapse
    }
}
