package xyz.sevive.arcaeaoffline.database.converters

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

object PlayResultConverters {
    @ColumnTypeConverter
    fun fromDatabaseValue(value: String?): PlayResult? = value?.let { Json.decodeFromString<PlayResult>(it) }

    @ColumnTypeConverter
    fun toDatabaseValue(value: PlayResult?): String? = value?.let { Json.encodeToString(value) }
}
