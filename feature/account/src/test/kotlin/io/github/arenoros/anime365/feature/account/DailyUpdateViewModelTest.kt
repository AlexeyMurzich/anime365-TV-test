package io.github.arenoros.anime365.feature.account

import io.github.arenoros.anime365.core.data.repository.AppUpdate
import io.github.arenoros.anime365.core.testing.FakeUpdateRepository
import io.github.arenoros.anime365.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class DailyUpdateViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun laterHidesDialog() {
        val update = AppUpdate("1.0.0", "Что нового", "https://example.org/anime365.apk")
        val viewModel = DailyUpdateViewModel(FakeUpdateRepository().apply { result = Result.success(update) })

        assertEquals(update, viewModel.update.value)
        viewModel.dismiss()
        assertNull(viewModel.update.value)
    }
}
