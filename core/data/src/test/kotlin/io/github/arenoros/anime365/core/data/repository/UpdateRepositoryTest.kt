package io.github.arenoros.anime365.core.data.repository

import androidx.test.core.app.ApplicationProvider
import io.github.arenoros.anime365.core.data.FakeSettingsStore
import io.github.arenoros.anime365.core.data.MutableClock
import io.github.arenoros.anime365.core.network.GitHubApi
import io.github.arenoros.anime365.core.network.model.ReleaseAssetDto
import io.github.arenoros.anime365.core.network.model.ReleaseDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

/** Robolectric — ради версии установленного приложения и `Build.SUPPORTED_ABIS`. */
@RunWith(RobolectricTestRunner::class)
class UpdateRepositoryTest {
    private val api = FakeGitHubApi()
    private val settings = FakeSettingsStore()
    private val clock = MutableClock()
    private val repository = GitHubUpdateRepository(api, settings, clock, ApplicationProvider.getApplicationContext())

    @Test
    fun tagGivesSameVersionCodeAsBuild() {
        assertEquals(100L, versionCode("v0.1.0"))
        assertEquals(10203L, versionCode("v1.2.3"))
        assertNull(versionCode("1.2.3"))
        assertNull(versionCode("v1.2"))
    }

    @Test
    fun apkFollowsDeviceAbiPreference() {
        val update = newerRelease(release("v1.0.0"), installedVersionCode = 100, abis = listOf("x86_64", "arm64-v8a"))

        assertEquals("1.0.0", update?.version)
        assertEquals("$DOWNLOADS/anime365-1.0.0-x86_64.apk", update?.url)
    }

    @Test
    fun releasePageWhenNoApkForDevice() {
        val update = newerRelease(release("v1.0.0"), installedVersionCode = 100, abis = listOf("x86"))

        assertEquals("https://github.com/Arenoros/anime365/releases/tag/v1.0.0", update?.url)
    }

    @Test
    fun sameOrOlderReleaseIsNoUpdate() {
        assertNull(newerRelease(release("v1.0.0"), installedVersionCode = 10000, abis = listOf("arm64-v8a")))
        assertNull(newerRelease(release("v0.9.0"), installedVersionCode = 10000, abis = listOf("arm64-v8a")))
        assertNull(newerRelease(release("nightly"), installedVersionCode = 0, abis = listOf("arm64-v8a")))
    }

    @Test
    fun launchChecksGitHubOncePerDay() =
        runTest {
            api.release = release("v99.0.0")

            assertEquals("99.0.0", repository.checkDaily()?.version)
            clock.advanceMinutes(23 * 60)
            assertNull(repository.checkDaily())
            clock.advanceMinutes(60)
            assertEquals("99.0.0", repository.checkDaily()?.version)

            assertEquals(2, api.calls)
        }

    @Test
    fun failedLaunchCheckIsRetriedOnNextLaunch() =
        runTest {
            assertNull(repository.checkDaily())
            api.release = release("v99.0.0")

            assertEquals("99.0.0", repository.checkDaily()?.version)
        }

    private fun release(tag: String) =
        ReleaseDto(
            tagName = tag,
            htmlUrl = "https://github.com/Arenoros/anime365/releases/tag/$tag",
            body = "  Что нового  ",
            assets =
                listOf("arm64-v8a", "armeabi-v7a", "x86_64").map { abi ->
                    val name = "anime365-${tag.removePrefix("v")}-$abi.apk"
                    ReleaseAssetDto(name, "$DOWNLOADS/$name")
                },
        )

    private class FakeGitHubApi : GitHubApi {
        var release: ReleaseDto? = null
        var calls = 0

        override suspend fun latestRelease(): ReleaseDto {
            calls++
            return release ?: throw IOException("offline")
        }
    }

    private companion object {
        const val DOWNLOADS = "https://github.com/Arenoros/anime365/releases/download"
    }
}
