package io.github.arenoros.anime365.core.network

import io.github.arenoros.anime365.core.network.model.ReleaseDto
import retrofit2.http.GET

const val GITHUB_API_URL = "https://api.github.com/"

/** Релизы приложения (ADR 0006). Без токена GitHub даёт 60 запросов в час с адреса — проверке раз в сутки хватает. */
interface GitHubApi {
    /** Последний опубликованный релиз; черновики и пре-релизы сюда не попадают. */
    @GET("repos/Arenoros/anime365/releases/latest")
    suspend fun latestRelease(): ReleaseDto
}
