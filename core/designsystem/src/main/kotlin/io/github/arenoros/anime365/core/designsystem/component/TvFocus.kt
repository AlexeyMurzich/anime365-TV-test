package io.github.arenoros.anime365.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** True only for the underlying app content while its separate player layer is expanded. */
val LocalTvContentCovered = staticCompositionLocalOf { false }

/** Keep remote-specific behaviour out of the touch interface. */
@Composable
fun isTelevision(): Boolean =
    LocalConfiguration.current.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION

/** Two-tone focus stays visible on light surfaces, dark surfaces and poster images. */
fun Modifier.tvFocusIndicator(): Modifier =
    composed {
        if (!isTelevision()) return@composed this
        var focused by remember { mutableStateOf(false) }
        this
            .onFocusChanged { focused = it.isFocused }
            .then(
                if (focused) {
                    Modifier
                        .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                        .border(3.dp, Color.Black, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                },
            )
    }

/** Save only the activated item's focus, alongside its navigation entry's saved scroll position. */
fun Modifier.tvClickable(
    onClick: () -> Unit,
    role: Role? = null,
): Modifier =
    composed {
        if (!isTelevision()) return@composed clickable(role = role, onClick = onClick)
        val requester = remember { FocusRequester() }
        var restore by rememberSaveable { mutableStateOf(false) }
        val windowFocused = LocalWindowInfo.current.isWindowFocused
        val covered = LocalTvContentCovered.current
        LaunchedEffect(windowFocused, covered) {
            if (restore && windowFocused && !covered) {
                withFrameNanos { }
                withFrameNanos { }
                requester.requestFocus()
                restore = false
            }
        }
        this.focusRequester(requester).tvFocusIndicator().clickable(role = role) {
            restore = true
            onClick()
        }
    }

/** Request focus once, after the asynchronously loaded primary action has entered composition. */
fun Modifier.tvInitialFocus(): Modifier =
    composed {
        if (!isTelevision()) return@composed this
        val requester = remember { FocusRequester() }
        val windowFocused = LocalWindowInfo.current.isWindowFocused
        val covered = LocalTvContentCovered.current
        var requested by remember { mutableStateOf(false) }
        LaunchedEffect(windowFocused, covered) {
            if (covered) requested = false
            if (windowFocused && !covered && !requested) {
                withFrameNanos { }
                requester.requestFocus()
                requested = true
            }
        }
        this.focusRequester(requester)
    }
