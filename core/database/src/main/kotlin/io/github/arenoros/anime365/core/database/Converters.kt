package io.github.arenoros.anime365.core.database

import androidx.room.TypeConverter
import io.github.arenoros.anime365.core.database.model.DescriptionEntry
import io.github.arenoros.anime365.core.database.model.GenreEntry
import io.github.arenoros.anime365.core.database.model.LinkEntry
import kotlinx.serialization.json.Json

internal class Converters {
    @TypeConverter
    fun genresToJson(value: List<GenreEntry>): String = Json.encodeToString(value)

    @TypeConverter
    fun genresFromJson(value: String): List<GenreEntry> = Json.decodeFromString(value)

    @TypeConverter
    fun descriptionsToJson(value: List<DescriptionEntry>): String = Json.encodeToString(value)

    @TypeConverter
    fun descriptionsFromJson(value: String): List<DescriptionEntry> = Json.decodeFromString(value)

    @TypeConverter
    fun linksToJson(value: List<LinkEntry>): String = Json.encodeToString(value)

    @TypeConverter
    fun linksFromJson(value: String): List<LinkEntry> = Json.decodeFromString(value)
}
