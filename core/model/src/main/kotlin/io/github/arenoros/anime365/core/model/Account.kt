package io.github.arenoros.anime365.core.model

import java.time.Instant

sealed interface Account {
    data object Guest : Account

    /** [subscriptionUntil] — `null`, если Подписки нет и не было. */
    data class User(
        val name: String,
        val subscriptionUntil: Instant?,
    ) : Account {
        fun hasSubscription(now: Instant): Boolean = subscriptionUntil?.isAfter(now) == true
    }
}
