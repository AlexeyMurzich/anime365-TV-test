package io.github.arenoros.anime365.core.database.library

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Аниме, которое пользователь смотрел. Название и постер — копией из кэша: кэш можно стереть,
 * Библиотеку — нет (ADR 0001). [translationType] и [team] — запомненный Вид перевода и Команда.
 */
@Entity(tableName = "library_series")
data class LibrarySeriesEntity(
    @PrimaryKey val seriesId: Int,
    val title: String,
    val posterUrl: String?,
    val translationType: String?,
    val team: String?,
    val lastEpisodeId: Int?,
    val lastTranslationId: Int?,
    val lastWatchedAt: Long?,
)

/** Прогресс Эпизода. [label] — копией, по той же причине, что название аниме. */
@Entity(tableName = "episode_progress", indices = [Index("seriesId")])
data class EpisodeProgressEntity(
    @PrimaryKey val episodeId: Int,
    val seriesId: Int,
    val label: String,
    val positionMs: Long,
    val durationMs: Long,
    val watched: Boolean,
    val updatedAt: Long,
)

/** Последний Эпизод аниме вместе с его Прогрессом. */
data class LastWatchedRow(
    val seriesId: Int,
    val title: String,
    val posterUrl: String?,
    val episodeId: Int,
    val label: String,
    val translationId: Int,
    val positionMs: Long,
    val durationMs: Long,
    val watched: Boolean,
)
