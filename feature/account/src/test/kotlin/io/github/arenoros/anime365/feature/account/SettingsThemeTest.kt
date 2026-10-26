package io.github.arenoros.anime365.feature.account

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.arenoros.anime365.core.datastore.AppSettings
import io.github.arenoros.anime365.core.datastore.ThemeMode
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsThemeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun themeChoicesUpdateOnlyTheThemeAndReflectTheSelection() {
        val original = AppSettings(wifiHeight = 480, adultContent = true)
        val settings = mutableStateOf(original)
        compose.setContent {
            KadrTheme {
                SettingsScreen(
                    settings = settings.value,
                    onUpdate = { transform -> settings.value = transform(settings.value) },
                    onEnableNotifications = {},
                    onBack = {},
                )
            }
        }
        compose.onNodeWithText("Как в системе").assertIsSelected()
        listOf("Светлая" to ThemeMode.Light, "Тёмная" to ThemeMode.Dark, "Как в системе" to ThemeMode.System)
            .forEach { (label, mode) ->
                compose.onNodeWithText(label).performClick().assertIsSelected()
                compose.runOnIdle { assertEquals(original.copy(themeMode = mode), settings.value) }
            }
    }
}
