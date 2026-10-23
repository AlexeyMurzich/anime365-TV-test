package io.github.arenoros.anime365.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.arenoros.anime365.core.database.model.FeedEpisodeEntity
import io.github.arenoros.anime365.core.database.model.FeedEpisodeWithSeries
import io.github.arenoros.anime365.core.database.model.ListStateEntity
import io.github.arenoros.anime365.core.database.model.SeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FeedDao {
    @Transaction
    @Query("SELECT * FROM feed_episodes ORDER BY releasedAt DESC LIMIT :limit")
    abstract fun observe(limit: Int): Flow<List<FeedEpisodeWithSeries>>

    /** Лента — снимок: каждое обновление заменяет её целиком. */
    @Transaction
    open suspend fun replace(
        state: ListStateEntity,
        series: List<SeriesEntity>,
        episodes: List<FeedEpisodeEntity>,
    ) {
        upsertSeries(series)
        deleteAll()
        insert(episodes)
        upsertState(state)
    }

    @Upsert
    protected abstract suspend fun upsertSeries(series: List<SeriesEntity>)

    @Upsert
    protected abstract suspend fun upsertState(state: ListStateEntity)

    @Query("DELETE FROM feed_episodes")
    protected abstract suspend fun deleteAll()

    @Insert
    protected abstract suspend fun insert(episodes: List<FeedEpisodeEntity>)
}
