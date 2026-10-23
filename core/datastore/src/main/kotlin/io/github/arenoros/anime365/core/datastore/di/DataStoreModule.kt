package io.github.arenoros.anime365.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.arenoros.anime365.core.datastore.AccountStore
import io.github.arenoros.anime365.core.datastore.DataStoreAccountStore
import io.github.arenoros.anime365.core.datastore.DataStoreSettingsStore
import io.github.arenoros.anime365.core.datastore.SettingsStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataStoreModule {
    @Binds
    abstract fun accountStore(store: DataStoreAccountStore): AccountStore

    @Binds
    abstract fun settingsStore(store: DataStoreSettingsStore): SettingsStore

    companion object {
        /** Имя файла — в правилах резервного копирования :app (data_extraction_rules.xml). */
        @Provides
        @Singleton
        fun accountDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> = PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("account") }
    }
}
