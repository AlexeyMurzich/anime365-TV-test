package io.github.arenoros.anime365.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Настройки и одноразовые подсказки. Входят в резервную копию Android. */
interface SettingsStore {
    /** Пояснение «Список хранится только на этом устройстве» уже показано. */
    val libraryHintShown: Flow<Boolean>

    suspend fun setLibraryHintShown()
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

        private companion object {
            val LIBRARY_HINT_SHOWN = booleanPreferencesKey("library_hint_shown")
        }
    }
