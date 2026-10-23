package io.github.arenoros.anime365.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.github.arenoros.anime365.core.data.catalog.CATALOG_PAGE_SIZE
import io.github.arenoros.anime365.core.data.catalog.CatalogQuery
import io.github.arenoros.anime365.core.data.catalog.CatalogRemoteMediator
import io.github.arenoros.anime365.core.data.toSummary
import io.github.arenoros.anime365.core.database.dao.SeriesListDao
import io.github.arenoros.anime365.core.model.CatalogFilter
import io.github.arenoros.anime365.core.model.SeriesSummary
import io.github.arenoros.anime365.core.network.Anime365Api
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

interface CatalogRepository {
    /** [query] — текст поиска; пустой — весь Каталог по [filter]. Порядок — по рейтингу. */
    fun catalog(
        query: String,
        filter: CatalogFilter,
    ): Flow<PagingData<SeriesSummary>>
}

internal class OfflineFirstCatalogRepository
    @Inject
    constructor(
        private val api: Anime365Api,
        private val dao: SeriesListDao,
        private val clock: Clock,
    ) : CatalogRepository {
        @OptIn(ExperimentalPagingApi::class)
        override fun catalog(
            query: String,
            filter: CatalogFilter,
        ): Flow<PagingData<SeriesSummary>> {
            val catalogQuery = CatalogQuery(query.trim().replace(WHITESPACE, " "), filter)
            return Pager(
                config = PagingConfig(pageSize = CATALOG_PAGE_SIZE, enablePlaceholders = false),
                remoteMediator = CatalogRemoteMediator(catalogQuery, api, dao, clock),
                pagingSourceFactory = { dao.pagingSource(catalogQuery.listKey) },
            ).flow.map { page -> page.map { it.toSummary() } }
        }

        private companion object {
            val WHITESPACE = Regex("\\s+")
        }
    }
