package io.github.arenoros.anime365.feature.catalog

import io.github.arenoros.anime365.core.model.NewEpisode
import io.github.arenoros.anime365.core.model.ReleaseStatus
import io.github.arenoros.anime365.core.model.SeriesSummary
import java.time.Instant

fun summary(
    id: Int,
    title: String = "Аниме $id",
) = SeriesSummary(
    id = id,
    title = title,
    originalTitle = "Anime $id",
    posterUrl = null,
    kind = "ТВ сериал",
    episodeCount = 12,
    year = 2026,
    season = "Осень 2026",
    score = 8.5,
    status = ReleaseStatus.Ongoing,
)

fun newEpisode(
    id: Int,
    releasedAt: String,
) = NewEpisode(
    episodeId = id,
    seriesId = 1,
    seriesTitle = "Фрирен",
    posterUrl = null,
    label = "$id серия",
    releasedAt = Instant.parse(releasedAt),
)
