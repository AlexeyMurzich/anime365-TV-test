package io.github.arenoros.anime365.core.model

/** Прогресс Эпизода: принадлежит Эпизоду, а не Переводу — смена Перевода его не сбрасывает. */
data class EpisodeProgress(
    val episodeId: Int,
    val positionMs: Long,
    val durationMs: Long,
    val watched: Boolean,
) {
    val fraction: Float get() = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
}

/** Эпизод, который смотрели последним, и Перевод, которым смотрели. */
data class LastWatched(
    val episodeId: Int,
    val episodeLabel: String,
    val translationId: Int,
    val progress: EpisodeProgress,
)

/** Карточка «Продолжить просмотр» на Главной. */
data class ContinueWatching(
    val seriesId: Int,
    val seriesTitle: String,
    val posterUrl: String?,
    val lastWatched: LastWatched,
)

/** Запомненный по аниме выбор: им отмечается Перевод следующего Эпизода. */
data class TranslationChoice(
    val kind: TranslationKind,
    val team: String,
)

/**
 * Перевод запомненной Команды того же Вида — лучше с тем же [source]: одна Команда даёт и TV, и BD.
 * Без Команды совпадение не определить.
 */
fun List<Translation>.ofTeam(
    choice: TranslationChoice,
    source: String? = null,
): Translation? {
    if (choice.team.isEmpty()) return null
    val candidates = filter { it.kind == choice.kind && it.team == choice.team }
    return candidates.firstOrNull { it.source == source } ?: candidates.firstOrNull()
}
