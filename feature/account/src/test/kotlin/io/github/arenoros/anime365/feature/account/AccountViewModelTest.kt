package io.github.arenoros.anime365.feature.account

import io.github.arenoros.anime365.core.model.Account
import io.github.arenoros.anime365.core.testing.FakeAccountRepository
import io.github.arenoros.anime365.core.testing.FakeLibraryTransfer
import io.github.arenoros.anime365.core.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import java.time.Clock

class AccountViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAccountRepository()

    @Test
    fun opensWithSubscriptionCheckOnSite() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = AccountViewModel(repository, FakeLibraryTransfer(), Clock.systemUTC())
            backgroundScope.launch { viewModel.uiState.collect {} }

            assertEquals(1, repository.refreshCalls)
            assertFalse(viewModel.uiState.value.isRefreshing)
        }

    @Test
    fun logOutTurnsUserIntoGuest() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.account.value = Account.User("Фрирен", null)
            val viewModel = AccountViewModel(repository, FakeLibraryTransfer(), Clock.systemUTC())
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.logOut()

            assertEquals(Account.Guest, viewModel.uiState.value.account)
        }
}
