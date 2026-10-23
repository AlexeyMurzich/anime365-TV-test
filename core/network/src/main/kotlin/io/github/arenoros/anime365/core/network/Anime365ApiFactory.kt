package io.github.arenoros.anime365.core.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

const val ANIME365_BASE_URL = "https://smotret-anime.app/api/"

// Спецификация просит называть себя в User-Agent.
private const val USER_AGENT = "anime365-android (+https://github.com/Arenoros/anime365)"

private val anime365Json =
    Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

/** Отдельно от Hilt-модуля, чтобы тесты собирали тот же клиент против MockWebServer. */
fun createAnime365Api(baseUrl: String = ANIME365_BASE_URL): Anime365Api {
    val client =
        OkHttpClient
            .Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain
                        .request()
                        .newBuilder()
                        .header("User-Agent", USER_AGENT)
                        .build(),
                )
            }.build()
    return Retrofit
        .Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(anime365Json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(Anime365Api::class.java)
}
