package io.github.arenoros.anime365.feature.account

import io.github.arenoros.anime365.core.datastore.AppSettings
import io.github.arenoros.anime365.core.testing.FakeSettingsStore
import io.github.arenoros.anime365.core.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val store = FakeSettingsStore()

    @Test
    fun changesAreSavedAndShown() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = SettingsViewModel(store)
            backgroundScope.launch { viewModel.settings.collect {} }
            assertEquals(AppSettings(), viewModel.settings.value)

            viewModel.update { it.copy(newEpisodeNotifications = false) }
            viewModel.update { it.copy(mobileHeight = 480) }

            val expected = AppSettings(newEpisodeNotifications = false, mobileHeight = 480)
            assertEquals(expected, store.settings.value)
            assertEquals(expected, viewModel.settings.value)
        }
}
