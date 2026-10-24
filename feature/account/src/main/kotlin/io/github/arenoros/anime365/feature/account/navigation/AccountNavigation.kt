package io.github.arenoros.anime365.feature.account.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import io.github.arenoros.anime365.feature.account.ProfileRoute
import io.github.arenoros.anime365.feature.account.SettingsRoute
import io.github.arenoros.anime365.feature.account.SubscriptionRoute
import io.github.arenoros.anime365.feature.account.login.LoginRoute
import kotlinx.serialization.Serializable

@Serializable
data object ProfileKey : NavKey

@Serializable
data object SettingsKey : NavKey

/** Вход через страницу сайта (ADR 0005). После входа экран закрывается сам. */
@Serializable
data object LoginKey : NavKey

/** Нужна Подписка: пояснение, а не ошибка. Закрывается сам, когда Подписка появилась. */
@Serializable
data object SubscriptionKey : NavKey

/** [onBack] закрывает и Вход, и Нужна Подписка — и после успеха, и по кнопке. */
fun EntryProviderScope<NavKey>.accountEntries(
    onLogin: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit,
) {
    entry<ProfileKey> { ProfileRoute(onLogin = onLogin, onOpenSettings = onOpenSettings) }
    entry<SettingsKey> { SettingsRoute(onBack = onBack) }
    entry<LoginKey> { LoginRoute(onDone = onBack, onClose = onBack) }
    entry<SubscriptionKey> { SubscriptionRoute(onDone = onBack, onBack = onBack) }
}
