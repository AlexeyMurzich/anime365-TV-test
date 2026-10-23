package io.github.arenoros.anime365.core.ui

import io.github.arenoros.anime365.core.model.Episode
import io.github.arenoros.anime365.core.model.SeriesDetails

// Типы, у которых Эпизоды нумеруются сеткой. Остальное — трейлеры, спешлы, фильмы — широкими кнопками.
private val NUMBERED_TYPES = setOf("tv", "ona", "ova")

private const val RANGE_SIZE = 50
private const val RANGES_FROM = 100

internal sealed interface EpisodeCell {
    val number: String

    data class Released(
        val episode: Episode,
    ) : EpisodeCell {
        override val number: String = episode.number.formatNumber()
    }

    /** Ещё не вышел: номер известен из числа Эпизодов, даты выхода в API нет. */
    data class Upcoming(
        override val number: String,
    ) : EpisodeCell
}

/**
 * Сетка Эпизодов экрана Аниме. Нумерованные — только Эпизоды основного типа аниме: OVA к ТВ-сериалу
 * со своим «1» иначе встала бы рядом с первой серией.
 */
internal data class EpisodeGrid(
    val special: List<Episode>,
    val numbered: List<EpisodeCell>,
) {
    /** Диапазоны «1–50», «51–100» — от 100 Эпизодов. */
    val ranges: List<IntRange> =
        if (numbered.size < RANGES_FROM) emptyList() else numbered.indices.chunked(RANGE_SIZE) { it.first()..it.last() }
}

internal fun SeriesDetails.episodeGrid(episodeCount: Int?): EpisodeGrid {
    val (numbered, special) = episodes.partition { it.type == type && it.type in NUMBERED_TYPES }
    val lastNumber = numbered.maxOfOrNull { it.number }?.toInt() ?: 0
    val upcoming =
        if (type in NUMBERED_TYPES && episodeCount != null && episodeCount > lastNumber) {
            (lastNumber + 1..episodeCount).map { EpisodeCell.Upcoming(it.toString()) }
        } else {
            emptyList()
        }
    return EpisodeGrid(special, numbered.map { EpisodeCell.Released(it) } + upcoming)
}

/** «12», «11.5». */
private fun Double.formatNumber(): String = if (this % 1.0 == 0.0) toLong().toString() else toString()
