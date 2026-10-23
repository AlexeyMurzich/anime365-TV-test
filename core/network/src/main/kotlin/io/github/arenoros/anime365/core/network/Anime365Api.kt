package io.github.arenoros.anime365.core.network

import io.github.arenoros.anime365.core.network.model.ApiResponse
import io.github.arenoros.anime365.core.network.model.SeriesDto
import io.github.arenoros.anime365.core.network.model.TranslationDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Публичное API anime365 (docs/anime365-openapi.yaml, поведение сверх спецификации — docs/api-notes.md).
 * Ошибки приходят с HTTP 200 и полем `error`, поэтому каждый ответ разворачивается через [ApiResponse.unwrap].
 */
interface Anime365Api {
    /**
     * Сортировка — по рейтингу MyAnimeList. [chips] и [query] не сочетаются: при `chips` сервер
     * игнорирует `query`.
     */
    @GET("series")
    suspend fun series(
        @Query("fields") fields: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int = 0,
        @Query("query") query: String? = null,
        @Query("chips") chips: String? = null,
        @Query("isAiring") isAiring: Int? = null,
        @Query("isActive") isActive: Int? = null,
        @Query("season") season: String? = null,
    ): ApiResponse<List<SeriesDto>>

    @GET("series/{id}")
    suspend fun series(
        @Path("id") id: Int,
        @Query("fields") fields: String,
    ): ApiResponse<SeriesDto>

    /** Лента `recent` отсортирована по `activeDateTime` от новых к старым; `afterId` она игнорирует. */
    @GET("translations")
    suspend fun translations(
        @Query("feed") feed: String,
        @Query("fields") fields: String,
        @Query("limit") limit: Int,
    ): ApiResponse<List<TranslationDto>>
}

/** Без `fields` в каждом элементе приходят все внешние ссылки и описания (docs/api-notes.md). */
object ApiFields {
    const val SERIES_SUMMARY =
        "id,titles,posterUrlSmall,typeTitle,type,year,season,myAnimeListScore,isAiring,isActive,numberOfEpisodes"

    /** Для поиска: фильтры к результатам `query` применяются на устройстве, им нужны жанры. */
    const val SERIES_SEARCH = "$SERIES_SUMMARY,genres"

    const val SERIES_DETAILS = "$SERIES_SUMMARY,posterUrl,url,genres,descriptions,links,episodes"

    const val FEED = "id,activeDateTime,seriesId,episode"
}
