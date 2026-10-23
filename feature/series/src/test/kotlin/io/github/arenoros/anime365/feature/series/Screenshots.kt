package io.github.arenoros.anime365.feature.series

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme

/**
 * Снимок экрана в `src/test/screenshots`. [fontScale] 2 — системный шрифт 200 %
 * (docs/roadmap.md, «Когда экран готов»).
 */
fun ComposeContentTestRule.captureScreen(
    name: String,
    fontScale: Float = 1f,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    setContent {
        val density = LocalDensity.current.density
        CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
            KadrTheme(darkTheme = darkTheme, content = content)
        }
    }
    waitForIdle()
    onRoot().captureRoboImage("src/test/screenshots/$name.png", ScreenshotOptions)
}

/** Эталоны записаны на Windows, CI сверяет на Linux: сглаживание шрифтов расходится на доли процента. */
val ScreenshotOptions = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
