package io.github.arenoros.anime365.core.data

import io.github.arenoros.anime365.core.network.Anime365Api
import io.github.arenoros.anime365.core.network.model.ApiResponse
import io.github.arenoros.anime365.core.network.model.SeriesDto
import io.github.arenoros.anime365.core.network.model.TranslationDto

/** Параметры запроса `/series`, как их увидел бы сервер. */
data class SeriesRequest(
    val fields: String,
    val limit: Int,
    val offset: Int,
    val query: String?,
    val chips: String?,
    val isAiring: Int?,
    val season: String?,
)

class FakeAnime365Api : Anime365Api {
    val seriesRequests = mutableListOf<SeriesRequest>()
    val detailsRequests = mutableListOf<Int>()
    var feedRequests = 0

    var seriesList: (SeriesRequest) -> List<SeriesDto> = { emptyList() }
    var seriesById: (Int) -> SeriesDto = { error("series $it not stubbed") }
    var feed: () -> List<TranslationDto> = { emptyList() }

    override suspend fun series(
        fields: String,
        limit: Int,
        offset: Int,
        query: String?,
        chips: String?,
        isAiring: Int?,
        isActive: Int?,
        season: String?,
    ): ApiResponse<List<SeriesDto>> {
        val request = SeriesRequest(fields, limit, offset, query, chips, isAiring, season)
        seriesRequests += request
        return ApiResponse(seriesList(request))
    }

    override suspend fun series(
        id: Int,
        fields: String,
    ): ApiResponse<SeriesDto> {
        detailsRequests += id
        return ApiResponse(seriesById(id))
    }

    override suspend fun translations(
        feed: String,
        fields: String,
        limit: Int,
    ): ApiResponse<List<TranslationDto>> {
        feedRequests++
        return ApiResponse(feed())
    }
}

fun seriesDto(
    id: Int,
    title: String = "Аниме $id",
    type: String = "tv",
    year: Int = 2026,
    season: String = "Осень 2026",
    score: Double = 8.0,
    isAiring: Int = 0,
    genres: List<Int> = emptyList(),
): SeriesDto =
    SeriesDto(
        id = id,
        titles = mapOf("ru" to title, "romaji" to "Anime $id"),
        type = type,
        typeTitle = "ТВ сериал",
        year = year,
        season = season,
        myAnimeListScore = score,
        isAiring = isAiring,
        genres =
            genres.map {
                io.github.arenoros.anime365.core.network.model
                    .GenreDto(it, "Жанр $it")
            },
    )
