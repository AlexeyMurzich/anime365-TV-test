package io.github.arenoros.anime365.core.model

/** Аниме в списках: столько, сколько нужно карточке и строке рейтинга. */
data class SeriesSummary(
    val id: Int,
    val title: String,
    val originalTitle: String?,
    val posterUrl: String?,
    val kind: String?,
    val episodeCount: Int?,
    val year: Int?,
    val season: String?,
    val score: Double?,
    val status: ReleaseStatus,
)

enum class ReleaseStatus { Ongoing, Released, Announced }

/**
 * Экран Аниме. Шапку можно показать сразу из [summary], закэшированного списком;
 * [details] — `null`, пока страница аниме ни разу не загружалась.
 */
data class Series(
    val summary: SeriesSummary,
    val details: SeriesDetails?,
)

data class SeriesDetails(
    val type: String?,
    val largePosterUrl: String?,
    val siteUrl: String?,
    val genres: List<Genre>,
    val descriptions: List<Description>,
    val links: List<ExternalLink>,
    val episodes: List<Episode>,
)

data class Genre(
    val id: Int,
    val title: String,
)

/** Описание из одного Источника: Шикимори, World Art и других. */
data class Description(
    val source: String,
    val text: String,
)

data class ExternalLink(
    val title: String,
    val url: String,
)

/**
 * [label] — подпись сайта: «12 серия», «ONA 4 серия», «Трейлер».
 * [number] дробный: бывают «11.5 серия».
 */
data class Episode(
    val id: Int,
    val number: Double,
    val label: String,
    val type: String,
)
