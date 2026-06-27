package xyz.sevive.arcaeaoffline.database.converters

import androidx.room.TypeConverter
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueUriType

object OcrQueueSourceTypeConverters {
    @TypeConverter
    fun fromDatabaseValue(value: Int?): OcrQueueUriType? =
        value?.let { OcrQueueUriType.entries.firstOrNull { e -> e.value == it } }

    @TypeConverter
    fun toDatabaseValue(value: OcrQueueUriType?): Int? = value?.value
}
