package xyz.sevive.arcaeaoffline.database.converters

import androidx.room3.ColumnTypeConverter
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingUriType

object OcrQueueStagingUriTypeConverters {
    @ColumnTypeConverter
    fun fromDatabaseValue(value: Int?): OcrQueueStagingUriType? =
        value?.let { OcrQueueStagingUriType.entries.firstOrNull { e -> e.value == it } }

    @ColumnTypeConverter
    fun toDatabaseValue(value: OcrQueueStagingUriType?): Int? = value?.value
}
