package io.github.arenoros.anime365.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.arenoros.anime365.core.data.repository.AccountRepository
import io.github.arenoros.anime365.core.data.repository.CatalogRepository
import io.github.arenoros.anime365.core.data.repository.HomeRepository
import io.github.arenoros.anime365.core.data.repository.JsonLibraryTransfer
import io.github.arenoros.anime365.core.data.repository.LibraryRepository
import io.github.arenoros.anime365.core.data.repository.LibraryTransfer
import io.github.arenoros.anime365.core.data.repository.LocalLibraryRepository
import io.github.arenoros.anime365.core.data.repository.OfflineFirstAccountRepository
import io.github.arenoros.anime365.core.data.repository.OfflineFirstCatalogRepository
import io.github.arenoros.anime365.core.data.repository.OfflineFirstHomeRepository
import io.github.arenoros.anime365.core.data.repository.OfflineFirstSeriesRepository
import io.github.arenoros.anime365.core.data.repository.OfflineFirstTranslationRepository
import io.github.arenoros.anime365.core.data.repository.SeriesRepository
import io.github.arenoros.anime365.core.data.repository.TranslationRepository
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun catalogRepository(repository: OfflineFirstCatalogRepository): CatalogRepository

    @Binds
    abstract fun homeRepository(repository: OfflineFirstHomeRepository): HomeRepository

    @Binds
    abstract fun seriesRepository(repository: OfflineFirstSeriesRepository): SeriesRepository

    @Binds
    abstract fun accountRepository(repository: OfflineFirstAccountRepository): AccountRepository

    @Binds
    abstract fun translationRepository(repository: OfflineFirstTranslationRepository): TranslationRepository

    @Binds
    abstract fun libraryRepository(repository: LocalLibraryRepository): LibraryRepository

    @Binds
    abstract fun libraryTransfer(transfer: JsonLibraryTransfer): LibraryTransfer

    companion object {
        @Provides
        fun clock(): Clock = Clock.systemUTC()
    }
}
