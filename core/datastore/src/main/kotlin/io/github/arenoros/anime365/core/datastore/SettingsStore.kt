package io.github.arenoros.anime365.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Экран Настроек (docs/screens.md). [wifiHeight] и [mobileHeight] — Разрешение, с которого Плеер начинает:
 * наибольшее не выше этого. По умолчанию 1080p по Wi-Fi и 720p по мобильной сети (docs/roadmap.md, M2).
 */
data class AppSettings(
    val newEpisodeNotifications: Boolean = true,
    val autoplayNext: Boolean = true,
    val wifiHeight: Int = 1080,
    val mobileHeight: Int = 720,
)

/** Настройки и одноразовые подсказки. Входят в резервную копию Android. */
interface SettingsStore {
    /** Пояснение «Список хранится только на этом устройстве» уже показано. */
    val libraryHintShown: Flow<Boolean>

    suspend fun setLibraryHintShown()

    val settings: Flow<AppSettings>

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings)
}

@Singleton
internal class DataStoreSettingsStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : SettingsStore {
        private val dataStore = PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") }

        override val libraryHintShown: Flow<Boolean> = dataStore.data.map { it[LIBRARY_HINT_SHOWN] == true }

        override suspend fun setLibraryHintShown() {
            dataStore.edit { it[LIBRARY_HINT_SHOWN] = true }
        }

        override val settings: Flow<AppSettings> = dataStore.data.map { it.toSettings() }

        override suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
            dataStore.edit { preferences ->
                val settings = transform(preferences.toSettings())
                preferences[NEW_EPISODE_NOTIFICATIONS] = settings.newEpisodeNotifications
                preferences[AUTOPLAY_NEXT] = settings.autoplayNext
                preferences[WIFI_HEIGHT] = settings.wifiHeight
                preferences[MOBILE_HEIGHT] = settings.mobileHeight
            }
        }

        private fun Preferences.toSettings(): AppSettings {
            val defaults = AppSettings()
            return AppSettings(
                newEpisodeNotifications = this[NEW_EPISODE_NOTIFICATIONS] ?: defaults.newEpisodeNotifications,
                autoplayNext = this[AUTOPLAY_NEXT] ?: defaults.autoplayNext,
                wifiHeight = this[WIFI_HEIGHT] ?: defaults.wifiHeight,
                mobileHeight = this[MOBILE_HEIGHT] ?: defaults.mobileHeight,
            )
        }

        private companion object {
            val LIBRARY_HINT_SHOWN = booleanPreferencesKey("library_hint_shown")
            val NEW_EPISODE_NOTIFICATIONS = booleanPreferencesKey("new_episode_notifications")
            val AUTOPLAY_NEXT = booleanPreferencesKey("autoplay_next")
            val WIFI_HEIGHT = intPreferencesKey("wifi_height")
            val MOBILE_HEIGHT = intPreferencesKey("mobile_height")
        }
    }
