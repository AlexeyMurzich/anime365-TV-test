package io.github.arenoros.anime365.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class TranslationDto(
    val id: Int,
    val seriesId: Int,
    val activeDateTime: String? = null,
    val episode: EpisodeDto? = null,
)
