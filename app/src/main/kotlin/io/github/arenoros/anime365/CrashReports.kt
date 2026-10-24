package io.github.arenoros.anime365

import android.content.Context
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.android.core.SentryAndroid

/**
 * Отчёты о сбоях в Sentry — только по согласию, по умолчанию выключены (ADR 0006). Без имени
 * пользователя и Токена: SDK сам их не собирает, а Токен из query `access_token` вычищается, если адрес
 * запроса попал в текст исключения.
 */
internal object CrashReports {
    /** DSN есть только в релизах из CI (docs/release.md). */
    val available: Boolean = BuildConfig.SENTRY_DSN.isNotEmpty()

    fun enable(context: Context) {
        SentryAndroid.init(context) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.environment = BuildConfig.FLAVOR
            options.setBeforeSend { event, _ -> event.apply { scrub() } }
            options.setBeforeBreadcrumb { breadcrumb, _ ->
                breadcrumb.apply { message = message?.let(::withoutToken) }
            }
        }
    }

    fun disable() = Sentry.close()
}

private val ACCESS_TOKEN = Regex("""(access_token=)[^&\s"']+""")

internal fun withoutToken(text: String): String = ACCESS_TOKEN.replace(text, "$1[Filtered]")

private fun SentryEvent.scrub() {
    exceptions?.forEach { exception -> exception.value = exception.value?.let(::withoutToken) }
    message?.let { message ->
        message.formatted = message.formatted?.let(::withoutToken)
        message.message = message.message?.let(::withoutToken)
    }
}
