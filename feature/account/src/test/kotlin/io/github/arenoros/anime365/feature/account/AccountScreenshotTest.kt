package io.github.arenoros.anime365.feature.account

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import io.github.arenoros.anime365.core.model.Account
import io.github.arenoros.anime365.core.testing.captureScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Instant
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel7)
class AccountScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val now = Instant.parse("2026-10-23T12:00:00Z")
    private val moscow = ZoneId.of("Europe/Moscow")

    @Test
    fun guest() = profile("profile_guest", Account.Guest)

    @Test
    fun subscriber() = profile("profile_user", Account.User("Фрирен", Instant.parse("2027-03-15T10:00:00Z")))

    @Test
    fun subscriberLargeFont() =
        profile(
            "profile_user_font200",
            Account.User("Провожающая в последний путь", Instant.parse("2027-03-15T10:00:00Z")),
            2f,
        )

    @Test
    fun noSubscription() = profile("profile_no_subscription", Account.User("Фрирен", subscriptionUntil = null))

    @Test
    fun subscriptionRequired() =
        compose.captureScreen("subscription") {
            SubscriptionScreen(
                AccountUiState(Account.User("Фрирен", null)),
                stillMissing = true,
                onRefresh = {},
                onBack = {},
            )
        }

    private fun profile(
        name: String,
        account: Account,
        fontScale: Float = 1f,
    ) = compose.captureScreen(name, fontScale) {
        ProfileScreen(
            account,
            now,
            onLogin = {},
            onLogOut = {},
            onOpenSettings = {},
            onExport = {},
            onImport = {},
            zone = moscow,
        )
    }
}
