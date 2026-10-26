package io.github.arenoros.anime365

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.arenoros.anime365.core.datastore.SettingsStore
import io.github.arenoros.anime365.core.datastore.ThemeMode
import io.github.arenoros.anime365.core.designsystem.theme.KadrTheme
import io.github.arenoros.anime365.feature.account.DailyUpdateCheck
import io.github.arenoros.anime365.feature.library.notifications.EpisodeLink
import io.github.arenoros.anime365.ui.Anime365App
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/** С GitHub обновляется только prod: dev ставится рядом и в релизы не попадает (ADR 0006). */
internal const val CHECKS_UPDATES = BuildConfig.FLAVOR == "prod"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsStore: SettingsStore

    /** Касание уведомления о Новом Эпизоде, ещё не обработанное навигацией. */
    private val link = MutableStateFlow<EpisodeLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Под статус-баром всегда зелёная шапка Kadr или чёрный Плеер: значки только светлые.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        // После пересоздания Activity переход уже в восстановленном стеке.
        if (savedInstanceState == null) link.value = EpisodeLink.from(intent)
        setContent {
            val pending by link.collectAsStateWithLifecycle()
            val settings by settingsStore.settings.collectAsStateWithLifecycle(initialValue = null)
            val darkTheme =
                when (settings?.themeMode ?: ThemeMode.System) {
                    ThemeMode.System -> isSystemInDarkTheme()
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                }
            KadrTheme(darkTheme = darkTheme) {
                Anime365App(link = pending, onLinkHandled = { link.value = null })
                if (CHECKS_UPDATES) DailyUpdateCheck()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        link.value = EpisodeLink.from(intent)
    }
}
