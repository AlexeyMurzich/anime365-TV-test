package io.github.arenoros.anime365.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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

/** true только у приложения под развёрнутым Плеером: Плеер лежит над ним отдельным слоем. */
val LocalTvContentCovered = staticCompositionLocalOf { false }

/** Телевизор с пультом: поведение для пульта не должно задевать сенсорный интерфейс телефона. */
@Composable
fun isTelevision(): Boolean =
    LocalConfiguration.current.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION

/** Двухцветная рамка фокуса видна и на светлых, и на тёмных поверхностях, и на постерах. */
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

/** Нажатый элемент получает фокус обратно, когда его экран снова виден: после «Назад» или Плеера. */
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

/**
 * Выдан ли первый фокус экрана. Живёт на уровне экрана, выше ленивого списка: элемент, прокрученный
 * за край и вернувшийся, фокус не перехватывает.
 */
@Stable
class TvInitialFocus internal constructor() {
    internal var requested by mutableStateOf(false)
}

@Composable
fun rememberTvInitialFocus(): TvInitialFocus = remember { TvInitialFocus() }

/** Один раз отдаёт фокус главному действию, когда оно появится после загрузки, и снова — после Плеера. */
fun Modifier.tvInitialFocus(state: TvInitialFocus): Modifier =
    composed {
        if (!isTelevision()) return@composed this
        val requester = remember { FocusRequester() }
        val windowFocused = LocalWindowInfo.current.isWindowFocused
        val covered = LocalTvContentCovered.current
        LaunchedEffect(windowFocused, covered) {
            if (covered) state.requested = false
            if (windowFocused && !covered && !state.requested) {
                withFrameNanos { }
                requester.requestFocus()
                state.requested = true
            }
        }
        this.focusRequester(requester)
    }
