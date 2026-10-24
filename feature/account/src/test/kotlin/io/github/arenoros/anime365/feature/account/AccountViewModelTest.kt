package io.github.arenoros.anime365.feature.account

import io.github.arenoros.anime365.core.data.repository.AppUpdate
import io.github.arenoros.anime365.core.model.Account
import io.github.arenoros.anime365.core.testing.FakeAccountRepository
import io.github.arenoros.anime365.core.testing.FakeLibraryTransfer
import io.github.arenoros.anime365.core.testing.FakeUpdateRepository
import io.github.arenoros.anime365.core.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Clock

class AccountViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAccountRepository()

    @Test
    fun opensWithSubscriptionCheckOnSite() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel =
                AccountViewModel(repository, FakeLibraryTransfer(), Clock.systemUTC(), FakeUpdateRepository())
            backgroundScope.launch { viewModel.uiState.collect {} }

            assertEquals(1, repository.refreshCalls)
            assertFalse(viewModel.uiState.value.isRefreshing)
        }

    @Test
    fun logOutTurnsUserIntoGuest() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.account.value = Account.User("Фрирен", null)
            val viewModel =
                AccountViewModel(repository, FakeLibraryTransfer(), Clock.systemUTC(), FakeUpdateRepository())
            backgroundScope.launch { viewModel.uiState.collect {} }

            viewModel.logOut()

            assertEquals(Account.Guest, viewModel.uiState.value.account)
        }

    @Test
    fun updateCheckEndsWithDialogOrMessage() =
        runTest(mainDispatcherRule.dispatcher) {
            val updates = FakeUpdateRepository()
            val viewModel = AccountViewModel(repository, FakeLibraryTransfer(), Clock.systemUTC(), updates)
            val checks = mutableListOf<UpdateCheck>()
            backgroundScope.launch { viewModel.updateChecks.collect { checks += it } }
            val update = AppUpdate("1.0.0", "Что нового", "https://example.org/anime365.apk")

            viewModel.checkUpdates()
            updates.result = Result.success(update)
            viewModel.checkUpdates()
            updates.result = Result.failure(IOException("offline"))
            viewModel.checkUpdates()

            assertEquals(listOf(UpdateCheck.Latest, UpdateCheck.Available(update), UpdateCheck.Failed), checks)
        }
}
