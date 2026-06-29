package xyz.sevive.arcaeaoffline.database.converters

import androidx.room.TypeConverter
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingUriType

object OcrQueueStagingUriTypeConverters {
    @TypeConverter
    fun fromDatabaseValue(value: Int?): OcrQueueStagingUriType? =
        value?.let { OcrQueueStagingUriType.entries.firstOrNull { e -> e.value == it } }

    @TypeConverter
    fun toDatabaseValue(value: OcrQueueStagingUriType?): Int? = value?.value
}
