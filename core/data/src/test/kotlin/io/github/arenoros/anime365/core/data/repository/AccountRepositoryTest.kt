package io.github.arenoros.anime365.core.data.repository

import io.github.arenoros.anime365.core.data.FakeAccountStore
import io.github.arenoros.anime365.core.data.FakeAnime365Api
import io.github.arenoros.anime365.core.datastore.StoredAccount
import io.github.arenoros.anime365.core.model.Account
import io.github.arenoros.anime365.core.network.model.ApiException
import io.github.arenoros.anime365.core.network.model.UserDto
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class AccountRepositoryTest {
    private val api = FakeAnime365Api()
    private val store = FakeAccountStore()
    private val repository = OfflineFirstAccountRepository(api, store)

    @Test
    fun logInExchangesSessionForTokenAndRemembersSubscription() =
        runTest {
            api.sessionTokens["PHPSESSID=1"] = "token"
            api.users["token"] =
                UserDto(isLogined = true, name = "Фрирен", isPremium = true, premiumUntil = "2028-03-19 18:48:30")

            assertTrue(repository.logIn("PHPSESSID=1").isSuccess)

            // Время сайта — московское.
            val user = repository.account.first() as Account.User
            assertEquals("Фрирен", user.name)
            assertEquals(Instant.parse("2028-03-19T15:48:30Z"), user.subscriptionUntil)
            assertEquals("token", store.account.value?.accessToken)
        }

    @Test
    fun logInWithoutSiteSessionFails() =
        runTest {
            val result = repository.logIn("PHPSESSID=guest")

            assertTrue(result.exceptionOrNull() is ApiException)
            assertEquals(Account.Guest, repository.account.first())
        }

    @Test
    fun guestPremiumStubIsNoSubscription() =
        runTest {
            api.sessionTokens["c"] = "token"
            api.users["token"] = UserDto(isLogined = true, name = "Без подписки", premiumUntil = "2000-01-01 00:00:00")

            repository.logIn("c")

            assertNull((repository.account.first() as Account.User).subscriptionUntil)
        }

    @Test
    fun refreshLogsOutWhenTokenStoppedWorking() =
        runTest {
            store.save(StoredAccount("revoked", "Старое имя", null))

            assertTrue(repository.refresh().isSuccess)

            assertEquals(Account.Guest, repository.account.first())
        }
}
