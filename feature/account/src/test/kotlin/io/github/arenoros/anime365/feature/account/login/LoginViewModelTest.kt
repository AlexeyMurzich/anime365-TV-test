package io.github.arenoros.anime365.feature.account.login

import io.github.arenoros.anime365.core.testing.FakeAccountRepository
import io.github.arenoros.anime365.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAccountRepository()
    private val viewModel = LoginViewModel(repository)

    @Test
    fun loginFormItselfIsNotAReasonToAskForToken() =
        runTest(mainDispatcherRule.dispatcher) {
            viewModel.onPageFinished("https://smotret-anime.app/users/login", "PHPSESSID=1")
            viewModel.onPageFinished("https://oauth.vk.com/authorize", "PHPSESSID=1")
            viewModel.onPageFinished("https://smotret-anime.app/", null)

            assertEquals(emptyList<String>(), repository.logInCookies)
            assertEquals(LoginState.Browsing, viewModel.state.value)
        }

    @Test
    fun sitePageAfterLoginExchangesSessionForToken() =
        runTest(mainDispatcherRule.dispatcher) {
            viewModel.onPageFinished("https://smotret-anime.app/", "PHPSESSID=1")

            assertEquals(listOf("PHPSESSID=1"), repository.logInCookies)
            assertEquals(LoginState.Done, viewModel.state.value)
        }

    @Test
    fun guestSessionKeepsUserOnSite() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.logInResult = Result.failure(IllegalStateException("403"))

            viewModel.onPageFinished("https://smotret-anime.app/catalog", "PHPSESSID=guest")

            assertEquals(LoginState.Browsing, viewModel.state.value)
        }

    @Test
    fun networkFailureIsShownWithRetry() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.logInResult = Result.failure(IOException("offline"))

            viewModel.onPageFinished("https://smotret-anime.app/", "PHPSESSID=1")
            assertTrue(viewModel.state.value is LoginState.Failed)

            viewModel.retry()
            assertEquals(LoginState.Browsing, viewModel.state.value)
        }
}
