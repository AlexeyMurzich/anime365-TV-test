package io.github.arenoros.anime365.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.arenoros.anime365.core.database.model.ListStateEntity
import io.github.arenoros.anime365.core.database.model.SeriesEntity
import io.github.arenoros.anime365.core.database.model.SeriesListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SeriesListDao {
    @Query(
        "SELECT series.* FROM series_list_items JOIN series ON series.id = series_list_items.seriesId " +
            "WHERE series_list_items.listKey = :key ORDER BY series_list_items.position",
    )
    abstract fun pagingSource(key: String): PagingSource<Int, SeriesEntity>

    @Query(
        "SELECT series.* FROM series_list_items JOIN series ON series.id = series_list_items.seriesId " +
            "WHERE series_list_items.listKey = :key ORDER BY series_list_items.position",
    )
    abstract fun observe(key: String): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM list_state WHERE `key` = :key")
    abstract suspend fun state(key: String): ListStateEntity?

    /** Страница списка: при [replace] старое содержимое списка стирается в той же транзакции. */
    @Transaction
    open suspend fun save(
        state: ListStateEntity,
        series: List<SeriesEntity>,
        items: List<SeriesListItemEntity>,
        replace: Boolean,
    ) {
        if (replace) deleteItems(state.key)
        upsertSeries(series)
        upsertItems(items)
        upsertState(state)
    }

    /** Списки с префиксом [keyPrefix], не обновлявшиеся с [before]: старые запросы Каталога. */
    @Transaction
    open suspend fun deleteStale(
        keyPrefix: String,
        before: Long,
    ) {
        deleteStaleItems(keyPrefix, before)
        deleteStaleStates(keyPrefix, before)
    }

    @Upsert
    protected abstract suspend fun upsertSeries(series: List<SeriesEntity>)

    @Upsert
    protected abstract suspend fun upsertItems(items: List<SeriesListItemEntity>)

    @Upsert
    protected abstract suspend fun upsertState(state: ListStateEntity)

    @Query("DELETE FROM series_list_items WHERE listKey = :key")
    protected abstract suspend fun deleteItems(key: String)

    @Query(
        "DELETE FROM series_list_items WHERE listKey IN " +
            "(SELECT `key` FROM list_state WHERE `key` LIKE :keyPrefix || '%' AND fetchedAt < :before)",
    )
    protected abstract suspend fun deleteStaleItems(
        keyPrefix: String,
        before: Long,
    )

    @Query("DELETE FROM list_state WHERE `key` LIKE :keyPrefix || '%' AND fetchedAt < :before")
    protected abstract suspend fun deleteStaleStates(
        keyPrefix: String,
        before: Long,
    )
}
