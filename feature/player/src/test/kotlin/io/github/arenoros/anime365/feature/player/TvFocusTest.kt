package io.github.arenoros.anime365.feature.player

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.arenoros.anime365.core.designsystem.component.LocalTvContentCovered
import io.github.arenoros.anime365.core.designsystem.component.tvClickable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TvFocusTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun activatedCardRestoresFocusWhenReturningFromDetails() {
        val details = mutableStateOf(false)
        val first = FocusRequester()
        compose.setContent {
            val config =
                Configuration(LocalConfiguration.current).apply {
                    uiMode = Configuration.UI_MODE_TYPE_TELEVISION
                }
            CompositionLocalProvider(LocalConfiguration provides config) {
                MaterialTheme {
                    val holder = rememberSaveableStateHolder()
                    if (details.value) {
                        Box(Modifier.size(100.dp).testTag("details"))
                    } else {
                        holder.SaveableStateProvider("catalog") {
                            Row {
                                Box(
                                    Modifier.size(80.dp).focusRequester(first).testTag("first").tvClickable({
                                        details.value =
                                            true
                                    }),
                                )
                                Box(Modifier.size(80.dp).testTag("second").tvClickable({ details.value = true }))
                            }
                        }
                    }
                }
            }
        }
        compose.runOnIdle { first.requestFocus() }
        compose.onNodeWithTag("second").performClick()
        compose.runOnIdle { details.value = false }
        compose.onNodeWithTag("second").assertIsFocused()
    }

    @Test
    fun coveredScreenRestoresTheActivatedCardAfterPlayerCloses() {
        val covered = mutableStateOf(false)
        val second = FocusRequester()
        val player = FocusRequester()
        compose.setContent {
            val config =
                Configuration(LocalConfiguration.current).apply {
                    uiMode = Configuration.UI_MODE_TYPE_TELEVISION
                }
            CompositionLocalProvider(LocalConfiguration provides config, LocalTvContentCovered provides covered.value) {
                Box {
                    Row {
                        Box(Modifier.size(80.dp).testTag("first").tvClickable({}))
                        Box(
                            Modifier
                                .size(80.dp)
                                .focusRequester(second)
                                .testTag("second")
                                .tvClickable({ covered.value = true }),
                        )
                    }
                    if (covered.value) {
                        Box(
                            Modifier
                                .size(200.dp)
                                .focusRequester(player)
                                .testTag("player")
                                .focusable(),
                        )
                    }
                    LaunchedEffect(covered.value) {
                        if (covered.value) player.requestFocus()
                    }
                }
            }
        }
        compose.runOnIdle { second.requestFocus() }
        compose.onNodeWithTag("second").assertIsFocused()
        compose.onNodeWithTag("second").performClick()
        compose.onNodeWithTag("player").assertIsFocused()
        compose.runOnIdle { covered.value = false }
        compose.onNodeWithTag("second").assertIsFocused()
    }
}
