package io.github.arenoros.anime365.feature.player

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TvPlayerControlsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun hiddenOverlayReceivesDirectionKeyAndFocusesPlayButton() {
        val active = mutableStateOf(true)
        lateinit var focus: FocusManager
        compose.setContent {
            focus = LocalFocusManager.current
            val config =
                Configuration(LocalConfiguration.current).apply {
                    uiMode = Configuration.UI_MODE_TYPE_TELEVISION
                }
            CompositionLocalProvider(LocalConfiguration provides config) {
                MaterialTheme {
                    PlayerControls(
                        state = PlayerUiState(status = PlayerStatus.Playing),
                        player = null,
                        fullscreen = true,
                        onBack = {},
                        onChooseTranslation = {},
                        onOpenSettings = {},
                        onToggleFullscreen = {},
                        onPlayPrevious = {},
                        onPlayNext = {},
                        modifier = Modifier.testTag("player-root"),
                        initiallyVisible = false,
                        active = active.value,
                    )
                }
            }
        }
        compose.onNodeWithTag("player-root").assertIsFocused()
        compose.onNodeWithTag("player-root").performKeyInput { pressKey(Key.DirectionRight) }
        compose.onNodeWithContentDescription("Воспроизвести").assertIsDisplayed().assertIsFocused()
        repeat(4) {
            compose.onNodeWithTag("player-root").performKeyInput { pressKey(Key.Back) }
            compose.onNodeWithContentDescription("Воспроизвести").assertDoesNotExist()
            compose.onNodeWithTag("player-root").assertIsFocused()
            compose.onNodeWithTag("player-root").performKeyInput { pressKey(Key.DirectionRight) }
            compose.onNodeWithContentDescription("Воспроизвести").assertIsDisplayed().assertIsFocused()
        }
        compose.runOnIdle { active.value = false }
        compose.runOnIdle { focus.clearFocus(force = true) }
        compose.runOnIdle { active.value = true }
        compose.onNodeWithContentDescription("Воспроизвести").assertIsFocused()
    }
}
