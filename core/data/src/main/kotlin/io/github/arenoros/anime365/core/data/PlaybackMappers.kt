package io.github.arenoros.anime365.core.data

import io.github.arenoros.anime365.core.database.library.EpisodeProgressEntity
import io.github.arenoros.anime365.core.database.library.LastWatchedRow
import io.github.arenoros.anime365.core.database.model.TranslationEntity
import io.github.arenoros.anime365.core.model.ContinueWatching
import io.github.arenoros.anime365.core.model.EpisodeProgress
import io.github.arenoros.anime365.core.model.LastWatched
import io.github.arenoros.anime365.core.model.StreamQuality
import io.github.arenoros.anime365.core.model.Translation
import io.github.arenoros.anime365.core.model.TranslationKind
import io.github.arenoros.anime365.core.model.VideoStream
import io.github.arenoros.anime365.core.network.ANIME365_SITE_URL
import io.github.arenoros.anime365.core.network.model.EmbedDto
import io.github.arenoros.anime365.core.network.model.TranslationDto
import java.net.URI

internal fun TranslationDto.toEntity(
    episodeId: Int,
    position: Int,
): TranslationEntity =
    TranslationEntity(
        id = id,
        episodeId = episodeId,
        type = type,
        team = authorsSummary.trim(),
        source = qualityType?.takeIf(String::isNotBlank),
        height = height,
        position = position,
    )

internal fun TranslationEntity.toModel(): Translation =
    Translation(id, episodeId, TranslationKind.of(type), team, source, height)

/** Пустой Видеопоток — сбой на стороне сайта, а не «нечего смотреть». */
internal fun EmbedDto.toModel(): VideoStream {
    val qualities =
        stream
            .filter { it.urls.isNotEmpty() }
            .sortedByDescending { it.height }
            .map { StreamQuality(it.height, it.urls.first()) }
    check(qualities.isNotEmpty()) { "Empty video stream" }
    // Путь субтитров — от корня сайта, и отдаются они без Токена (docs/api-notes.md).
    return VideoStream(qualities, subtitlesUrl?.let { URI(ANIME365_SITE_URL).resolve(it).toString() })
}

internal fun EpisodeProgressEntity.toModel(): EpisodeProgress =
    EpisodeProgress(episodeId, positionMs, durationMs, watched)

internal fun LastWatchedRow.toModel(): ContinueWatching =
    ContinueWatching(
        seriesId = seriesId,
        seriesTitle = title,
        posterUrl = posterUrl,
        lastWatched =
            LastWatched(
                episodeId = episodeId,
                episodeLabel = label,
                translationId = translationId,
                progress = EpisodeProgress(episodeId, positionMs, durationMs, watched),
            ),
    )
