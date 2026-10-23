package io.github.arenoros.anime365.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.arenoros.anime365.core.network.Anime365Api
import io.github.arenoros.anime365.core.network.createAnime365Api
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Provides
    @Singleton
    fun anime365Api(): Anime365Api = createAnime365Api()
}
