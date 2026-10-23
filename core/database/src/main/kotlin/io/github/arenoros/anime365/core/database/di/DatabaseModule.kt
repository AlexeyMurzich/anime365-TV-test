package io.github.arenoros.anime365.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.arenoros.anime365.core.database.CacheDatabase
import io.github.arenoros.anime365.core.database.dao.FeedDao
import io.github.arenoros.anime365.core.database.dao.SeriesDao
import io.github.arenoros.anime365.core.database.dao.SeriesListDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun cacheDatabase(
        @ApplicationContext context: Context,
    ): CacheDatabase =
        Room
            .databaseBuilder(context, CacheDatabase::class.java, CacheDatabase.FILE_NAME)
            // Это кэш: при смене схемы его не жалко пересобрать с сервера.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun seriesDao(database: CacheDatabase): SeriesDao = database.seriesDao()

    @Provides
    fun seriesListDao(database: CacheDatabase): SeriesListDao = database.seriesListDao()

    @Provides
    fun feedDao(database: CacheDatabase): FeedDao = database.feedDao()
}
