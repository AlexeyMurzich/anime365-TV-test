package io.github.arenoros.anime365.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** `./gradlew :app:generateProdReleaseBaselineProfile` — профиль ляжет в app/src/main/generated. */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() =
        rule.collect(PACKAGE_NAME, includeInStartupProfile = true) {
            pressHome()
            startActivityAndWait()
            browse()
        }
}
