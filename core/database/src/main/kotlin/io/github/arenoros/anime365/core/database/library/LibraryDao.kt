package io.github.arenoros.anime365.core.database.library

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

private const val LAST_WATCHED_SELECT = """
    SELECT s.seriesId, s.title, s.posterUrl, p.episodeId, p.label, s.lastTranslationId AS translationId,
        p.positionMs, p.durationMs, p.watched
    FROM library_series s JOIN episode_progress p ON p.episodeId = s.lastEpisodeId
"""

@Dao
abstract class LibraryDao {
    @Query("SELECT * FROM episode_progress WHERE seriesId = :seriesId")
    abstract fun progress(seriesId: Int): Flow<List<EpisodeProgressEntity>>

    @Query("SELECT * FROM episode_progress WHERE episodeId = :episodeId")
    abstract suspend fun progressOf(episodeId: Int): EpisodeProgressEntity?

    @Query("$LAST_WATCHED_SELECT WHERE s.seriesId = :seriesId")
    abstract fun lastWatched(seriesId: Int): Flow<LastWatchedRow?>

    /** Недосмотренные Эпизоды, по одному на аниме, от последних к давним. */
    @Query("$LAST_WATCHED_SELECT WHERE p.watched = 0 AND p.positionMs > 0 ORDER BY s.lastWatchedAt DESC LIMIT :limit")
    abstract fun continueWatching(limit: Int): Flow<List<LastWatchedRow>>

    @Query("SELECT * FROM library_series WHERE seriesId = :seriesId")
    abstract suspend fun series(seriesId: Int): LibrarySeriesEntity?

    /**
     * Строку аниме обновляем по полям, а не заменяем: в M3 в ней появится Статус, и запись
     * Прогресса не должна его затирать.
     */
    @Transaction
    open suspend fun savePlayback(
        series: LibrarySeriesEntity,
        progress: EpisodeProgressEntity,
    ) {
        insertSeries(series)
        updateSeries(
            series.seriesId,
            series.title,
            series.posterUrl,
            series.translationType,
            series.team,
            series.lastEpisodeId,
            series.lastTranslationId,
            series.lastWatchedAt,
        )
        upsertProgress(progress)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertSeries(series: LibrarySeriesEntity)

    @Suppress("LongParameterList")
    @Query(
        """
        UPDATE library_series SET title = :title, posterUrl = :posterUrl, translationType = :translationType,
            team = :team, lastEpisodeId = :lastEpisodeId, lastTranslationId = :lastTranslationId,
            lastWatchedAt = :lastWatchedAt
        WHERE seriesId = :seriesId
        """,
    )
    protected abstract suspend fun updateSeries(
        seriesId: Int,
        title: String,
        posterUrl: String?,
        translationType: String?,
        team: String?,
        lastEpisodeId: Int?,
        lastTranslationId: Int?,
        lastWatchedAt: Long?,
    )

    @Upsert
    protected abstract suspend fun upsertProgress(progress: EpisodeProgressEntity)
}
