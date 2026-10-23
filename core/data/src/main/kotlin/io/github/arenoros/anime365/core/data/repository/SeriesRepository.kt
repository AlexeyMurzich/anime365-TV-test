package io.github.arenoros.anime365.core.data.repository

import io.github.arenoros.anime365.core.data.catchingFailures
import io.github.arenoros.anime365.core.data.toDetailsEntity
import io.github.arenoros.anime365.core.data.toEntity
import io.github.arenoros.anime365.core.data.toEpisodeEntities
import io.github.arenoros.anime365.core.data.toModel
import io.github.arenoros.anime365.core.database.dao.SeriesDao
import io.github.arenoros.anime365.core.model.Series
import io.github.arenoros.anime365.core.network.Anime365Api
import io.github.arenoros.anime365.core.network.ApiFields
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface SeriesRepository {
    /** `null` — аниме ещё ни разу не загружалось, даже в списке. */
    fun series(id: Int): Flow<Series?>

    /** Без [force] запрашивает сервер, только если страница аниме старше суток. */
    suspend fun refresh(
        id: Int,
        force: Boolean,
    ): Result<Unit>
}

private val SERIES_TTL_MILLIS = TimeUnit.HOURS.toMillis(24)

/** Аниме скрыто на сайте или у него нет названия: показывать нечего. */
class SeriesUnavailableException(
    id: Int,
) : IllegalStateException("Series $id is not available")

internal class OfflineFirstSeriesRepository
    @Inject
    constructor(
        private val api: Anime365Api,
        private val dao: SeriesDao,
        private val clock: Clock,
    ) : SeriesRepository {
        override fun series(id: Int): Flow<Series?> = dao.observe(id).map { it?.toModel() }.distinctUntilChanged()

        override suspend fun refresh(
            id: Int,
            force: Boolean,
        ): Result<Unit> =
            catchingFailures {
                val fetchedAt = dao.detailsFetchedAt(id)
                if (!force && fetchedAt != null &&
                    clock.millis() - fetchedAt < SERIES_TTL_MILLIS
                ) {
                    return@catchingFailures
                }
                val dto = api.series(id, ApiFields.SERIES_DETAILS).unwrap()
                val entity = dto.toEntity() ?: throw SeriesUnavailableException(id)
                dao.saveDetails(entity, dto.toDetailsEntity(clock.millis()), dto.toEpisodeEntities())
            }
    }
