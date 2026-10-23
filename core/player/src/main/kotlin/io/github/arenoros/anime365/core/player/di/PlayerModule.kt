package io.github.arenoros.anime365.core.player.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.arenoros.anime365.core.player.AnimePlayer
import io.github.arenoros.anime365.core.player.VideoPlayer

@Module
@InstallIn(SingletonComponent::class)
internal abstract class PlayerModule {
    @Binds
    abstract fun videoPlayer(player: AnimePlayer): VideoPlayer
}
