package io.github.arenoros.anime365.core.data.repository

import io.github.arenoros.anime365.core.data.toModel
import io.github.arenoros.anime365.core.database.library.EpisodeProgressEntity
import io.github.arenoros.anime365.core.database.library.LibraryDao
import io.github.arenoros.anime365.core.database.library.LibrarySeriesEntity
import io.github.arenoros.anime365.core.model.ContinueWatching
import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.EpisodeProgress
import io.github.arenoros.anime365.core.model.LastWatched
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationChoice
import io.github.arenoros.anime365.core.model.TranslationKind
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

/** Что играет и где остановились: из этого пишется Прогресс. */
data class Playback(
    val series: SeriesSummary,
    val episode: Episode,
    val translation: Translation,
    val positionMs: Long,
    val durationMs: Long,
)

/** Прогресс и запомненный выбор Перевода (ADR 0001: только на устройстве). */
interface LibraryRepository {
    /** По `id` Эпизода. */
    fun progress(seriesId: Int): Flow<Map<Int, EpisodeProgress>>

    fun lastWatched(seriesId: Int): Flow<LastWatched?>

    fun continueWatching(): Flow<List<ContinueWatching>>

    suspend fun progressOf(episodeId: Int): EpisodeProgress?

    suspend fun choice(seriesId: Int): TranslationChoice?

    /** Заодно запоминает Перевод: им будет отмечен Перевод следующего Эпизода. */
    suspend fun save(playback: Playback)
}

/** С этой доли Эпизод считается просмотренным: дальше обычно эндинг и анонс следующей серии. */
internal const val WATCHED_FRACTION = 0.9
private const val CONTINUE_WATCHING_LIMIT = 20

internal class LocalLibraryRepository
    @Inject
    constructor(
        private val dao: LibraryDao,
        private val clock: Clock,
    ) : LibraryRepository {
        override fun progress(seriesId: Int): Flow<Map<Int, EpisodeProgress>> =
            dao
                .progress(seriesId)
                .map { rows ->
                    rows.associate { it.episodeId to it.toModel() }
                }.distinctUntilChanged()

        override fun lastWatched(seriesId: Int): Flow<LastWatched?> =
            dao.lastWatched(seriesId).map { it?.toModel()?.lastWatched }.distinctUntilChanged()

        override fun continueWatching(): Flow<List<ContinueWatching>> =
            dao
                .continueWatching(CONTINUE_WATCHING_LIMIT)
                .map { rows ->
                    rows.map { it.toModel() }
                }.distinctUntilChanged()

        override suspend fun progressOf(episodeId: Int): EpisodeProgress? = dao.progressOf(episodeId)?.toModel()

        override suspend fun choice(seriesId: Int): TranslationChoice? {
            val series = dao.series(seriesId)
            return series?.translationType?.let { TranslationChoice(TranslationKind.of(it), series.team.orEmpty()) }
        }

        override suspend fun save(playback: Playback) {
            val now = clock.millis()
            val (series, episode, translation) = playback
            val reached = playback.durationMs > 0 && playback.positionMs >= playback.durationMs * WATCHED_FRACTION
            val watched = reached || dao.progressOf(episode.id)?.watched == true
            dao.savePlayback(
                LibrarySeriesEntity(
                    seriesId = series.id,
                    title = series.title,
                    posterUrl = series.posterUrl,
                    translationType = translation.kind.apiType,
                    team = translation.team,
                    lastEpisodeId = episode.id,
                    lastTranslationId = translation.id,
                    lastWatchedAt = now,
                ),
                EpisodeProgressEntity(
                    episodeId = episode.id,
                    seriesId = series.id,
                    label = episode.label,
                    positionMs = playback.positionMs,
                    durationMs = playback.durationMs,
                    watched = watched,
                    updatedAt = now,
                ),
            )
        }
    }
