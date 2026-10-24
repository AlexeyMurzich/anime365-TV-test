package io.github.arenoros.anime365.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Релиз на GitHub: [body] — «что нового» из сообщения тега (docs/release.md). */
@Serializable
data class ReleaseDto(
    @SerialName("tag_name") val tagName: String,
    @SerialName("html_url") val htmlUrl: String,
    val body: String = "",
    val assets: List<ReleaseAssetDto> = emptyList(),
)

/** APK релиза: `anime365-1.2.0-arm64-v8a.apk`. */
@Serializable
data class ReleaseAssetDto(
    val name: String,
    @SerialName("browser_download_url") val downloadUrl: String,
)
