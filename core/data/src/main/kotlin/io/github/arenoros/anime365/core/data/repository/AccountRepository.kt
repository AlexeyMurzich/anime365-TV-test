package io.github.arenoros.anime365.core.data.repository

import io.github.arenoros.anime365.core.data.catchingFailures
import io.github.arenoros.anime365.core.data.parseSiteDateTime
import io.github.arenoros.anime365.core.datastore.AccountStore
import io.github.arenoros.anime365.core.datastore.StoredAccount
import io.github.arenoros.anime365.core.model.Account
import io.github.arenoros.anime365.core.network.ANIME365_SITE_URL
import io.github.arenoros.anime365.core.network.Anime365Api
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

/** Страницы сайта, которые приложение открывает в браузере. */
object SiteLinks {
    const val SITE = ANIME365_SITE_URL

    /** Здесь же вход через ВКонтакте (ADR 0005). */
    const val LOGIN = "${ANIME365_SITE_URL}users/login"
}

interface AccountRepository {
    val account: Flow<Account>

    /** Обменивает сессию сайта из WebView на Токен доступа (ADR 0005). */
    suspend fun logIn(sessionCookie: String): Result<Unit>

    /** Перечитывает `/api/me`: Подписка могла появиться или кончиться, Токен — перестать работать. */
    suspend fun refresh(): Result<Unit>

    suspend fun logOut()
}

/** Вход не выполнен или Токен перестал работать — например, после смены пароля. */
class NotLoggedInException : IllegalStateException("Not logged in")

internal class OfflineFirstAccountRepository
    @Inject
    constructor(
        private val api: Anime365Api,
        private val store: AccountStore,
    ) : AccountRepository {
        override val account: Flow<Account> =
            store.account.map { it?.toModel() ?: Account.Guest }.distinctUntilChanged()

        override suspend fun logIn(sessionCookie: String): Result<Unit> =
            catchingFailures {
                val token = api.accessToken(sessionCookie).unwrap().accessToken
                if (!saveUser(token)) throw NotLoggedInException()
            }

        override suspend fun refresh(): Result<Unit> =
            catchingFailures {
                val token = store.account.first()?.accessToken ?: return@catchingFailures
                if (!saveUser(token)) store.clear()
            }

        override suspend fun logOut() = store.clear()

        /** `false` — сервер не узнал Токен: с недействительным Токеном `/api/me` отвечает гостем. */
        private suspend fun saveUser(token: String): Boolean {
            val user = api.me(token).unwrap()
            if (!user.isLogined) return false
            val until = if (user.isPremium) parseSiteDateTime(user.premiumUntil)?.toEpochMilli() else null
            store.save(StoredAccount(token, user.name, until))
            return true
        }
    }

private fun StoredAccount.toModel(): Account = Account.User(name, subscriptionUntil?.let(Instant::ofEpochMilli))
