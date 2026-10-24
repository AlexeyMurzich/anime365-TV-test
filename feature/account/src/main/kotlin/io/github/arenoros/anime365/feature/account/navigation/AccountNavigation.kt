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

/**
 * [onBack] закрывает и Вход, и Нужна Подписка — и после успеха, и по кнопке. [checksUpdates] и
 * [crashReportsAvailable] зависят от сборки: обновления — только у prod, отчёты — только с DSN Sentry.
 */
fun EntryProviderScope<NavKey>.accountEntries(
    onLogin: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit,
    checksUpdates: Boolean,
    crashReportsAvailable: Boolean,
) {
    entry<ProfileKey> {
        ProfileRoute(
            onLogin = onLogin,
            onOpenSettings = onOpenSettings,
            checksUpdates = checksUpdates,
        )
    }
    entry<SettingsKey> { SettingsRoute(onBack = onBack, crashReportsAvailable = crashReportsAvailable) }
    entry<LoginKey> { LoginRoute(onDone = onBack, onClose = onBack) }
    entry<SubscriptionKey> { SubscriptionRoute(onDone = onBack, onBack = onBack) }
}
