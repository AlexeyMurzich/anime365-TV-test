package io.github.arenoros.anime365.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** [priority] — порядок сайта: больше — выше. [height] — Разрешение. */
@Serializable
data class TranslationDto(
    val id: Int,
    val seriesId: Int = 0,
    val episodeId: Int = 0,
    val activeDateTime: String? = null,
    val episode: EpisodeDto? = null,
    val type: String = "",
    val authorsSummary: String = "",
    val qualityType: String? = null,
    val height: Int = 0,
    val priority: Int = 0,
)

/** Видеопоток (docs/api-notes.md): `subtitlesUrl` — путь от корня сайта. */
@Serializable
data class EmbedDto(
    val stream: List<StreamDto> = emptyList(),
    val subtitlesUrl: String? = null,
)

/** [urls] — зеркала одного файла. */
@Serializable
data class StreamDto(
    val height: Int,
    val urls: List<String> = emptyList(),
)

/** У гостя `premiumUntil` — заглушка `2000-01-01 00:00:00`. */
@Serializable
data class UserDto(
    val isLogined: Boolean = false,
    val name: String = "",
    val isPremium: Boolean = false,
    val premiumUntil: String? = null,
)

@Serializable
data class AccessTokenDto(
    @SerialName("access_token") val accessToken: String,
)
