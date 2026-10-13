package xyz.sevive.arcaeaoffline.database.converters

import androidx.room3.ColumnTypeConverter
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus

object OcrQueueTaskStatusConverters {
    @ColumnTypeConverter
    fun fromDatabaseValue(value: Int?): OcrQueueTaskStatus? = value?.let { OcrQueueTaskStatus.entries[it] }

    @ColumnTypeConverter
    fun toDatabaseValue(value: OcrQueueTaskStatus?): Int? = value?.value
}
