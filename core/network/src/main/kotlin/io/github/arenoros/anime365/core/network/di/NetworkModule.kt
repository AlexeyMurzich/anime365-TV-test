package io.github.arenoros.anime365.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.arenoros.anime365.core.network.Anime365Api
import io.github.arenoros.anime365.core.network.createAnime365Api
import io.github.arenoros.anime365.core.network.createOkHttpClient
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {
    @Provides
    @Singleton
    fun okHttpClient(): OkHttpClient = createOkHttpClient()

    @Provides
    @Singleton
    fun anime365Api(client: OkHttpClient): Anime365Api = createAnime365Api(client = client)
}
