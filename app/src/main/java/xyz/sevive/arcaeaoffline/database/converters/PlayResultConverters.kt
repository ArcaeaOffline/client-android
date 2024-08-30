package xyz.sevive.arcaeaoffline.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult


object PlayResultConverters {
    @TypeConverter
    fun fromDatabaseValue(value: String?): PlayResult? {
        return value?.let { Json.decodeFromString<PlayResult>(it) }
    }

    @TypeConverter
    fun toDatabaseValue(value: PlayResult?): String? {
        return value?.let { Json.encodeToString(value) }
    }
}
