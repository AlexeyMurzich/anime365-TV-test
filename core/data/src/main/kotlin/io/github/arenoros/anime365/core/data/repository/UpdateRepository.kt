package io.github.arenoros.anime365.core.data.repository

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.arenoros.anime365.core.data.catchingFailures
import io.github.arenoros.anime365.core.datastore.SettingsStore
import io.github.arenoros.anime365.core.network.GitHubApi
import io.github.arenoros.anime365.core.network.model.ReleaseDto
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Duration
import javax.inject.Inject

/** Новая версия на GitHub. [url] — APK под архитектуру устройства, а если такого нет — страница релиза. */
data class AppUpdate(
    val version: String,
    val notes: String,
    val url: String,
)

/** Обновления приходят из GitHub Releases, а не из магазина (ADR 0006). */
interface UpdateRepository {
    /** `null` — установлена последняя версия. */
    suspend fun check(): Result<AppUpdate?>

    /** Проверка при запуске: не чаще раза в сутки; ошибка сети — то же, что «обновлений нет». */
    suspend fun checkDaily(): AppUpdate?
}

internal class GitHubUpdateRepository
    @Inject
    constructor(
        private val api: GitHubApi,
        private val settings: SettingsStore,
        private val clock: Clock,
        @ApplicationContext private val context: Context,
    ) : UpdateRepository {
        override suspend fun check(): Result<AppUpdate?> =
            catchingFailures {
                newerRelease(
                    api.latestRelease(),
                    installedVersionCode(),
                    Build.SUPPORTED_ABIS.toList(),
                )
            }

        override suspend fun checkDaily(): AppUpdate? {
            val now = clock.instant()
            val checkedAt = settings.updateCheckedAt.first()
            if (checkedAt != null && Duration.between(checkedAt, now) < CHECK_INTERVAL) return null
            return check().onSuccess { settings.setUpdateCheckedAt(now) }.getOrNull()
        }

        // getPackageInfo(String, PackageInfoFlags) — только с API 33, longVersionCode — с API 28.
        @Suppress("DEPRECATION")
        private fun installedVersionCode(): Long {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            return if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.P
            ) {
                info.longVersionCode
            } else {
                info.versionCode.toLong()
            }
        }

        private companion object {
            val CHECK_INTERVAL: Duration = Duration.ofDays(1)
        }
    }

/** Тег `v1.2.0` — тот же versionCode, что у сборки (app/build.gradle.kts): 10200. `null` — тег не версии. */
internal fun versionCode(tag: String): Long? {
    val (major, minor, patch) =
        Regex("""v(\d+)\.(\d+)\.(\d+)""").matchEntire(tag)?.destructured ?: return null
    return major.toLong() * MAJOR_STEP + minor.toLong() * MINOR_STEP + patch.toLong()
}

private const val MAJOR_STEP = 10_000L
private const val MINOR_STEP = 100L

/** [abis] — по убыванию предпочтения, как `Build.SUPPORTED_ABIS`. */
internal fun newerRelease(
    release: ReleaseDto,
    installedVersionCode: Long,
    abis: List<String>,
): AppUpdate? {
    val code = versionCode(release.tagName)
    if (code == null || code <= installedVersionCode) return null
    val apk = abis.firstNotNullOfOrNull { abi -> release.assets.firstOrNull { it.name.endsWith("-$abi.apk") } }
    return AppUpdate(release.tagName.removePrefix("v"), release.body.trim(), apk?.downloadUrl ?: release.htmlUrl)
}
