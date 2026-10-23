package io.github.arenoros.anime365.core.model

import java.time.Instant

/** Строка блока «Новые серии»: один Эпизод, сколько бы Переводов у него ни вышло. */
data class FeedEpisode(
    val episodeId: Int,
    val seriesId: Int,
    val seriesTitle: String,
    val posterUrl: String?,
    val label: String,
    val releasedAt: Instant,
)
