package io.github.arenoros.anime365.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import io.github.arenoros.anime365.core.database.model.ListStateEntity
import io.github.arenoros.anime365.core.database.model.TranslationEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TranslationDao {
    @Query("SELECT * FROM translations WHERE episodeId = :episodeId ORDER BY position")
    abstract fun observe(episodeId: Int): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translations WHERE episodeId = :episodeId ORDER BY position")
    abstract suspend fun get(episodeId: Int): List<TranslationEntity>

    @Query("SELECT fetchedAt FROM list_state WHERE `key` = :key")
    abstract suspend fun fetchedAt(key: String): Long?

    @Transaction
    open suspend fun replace(
        episodeId: Int,
        translations: List<TranslationEntity>,
        state: ListStateEntity,
    ) {
        delete(episodeId)
        insert(translations)
        upsertState(state)
    }

    @Query("DELETE FROM translations WHERE episodeId = :episodeId")
    protected abstract suspend fun delete(episodeId: Int)

    @Insert
    protected abstract suspend fun insert(translations: List<TranslationEntity>)

    @Upsert
    protected abstract suspend fun upsertState(state: ListStateEntity)
}
