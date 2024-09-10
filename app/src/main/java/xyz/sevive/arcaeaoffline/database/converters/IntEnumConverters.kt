package xyz.sevive.arcaeaoffline.database.converters

import androidx.room.TypeConverter
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus


object OcrQueueTaskStatusConverters {
    @TypeConverter
    fun fromDatabaseValue(value: Int?): OcrQueueTaskStatus? {
        return value?.let { OcrQueueTaskStatus.entries[it] }
    }

    @TypeConverter
    fun toDatabaseValue(value: OcrQueueTaskStatus?): Int? {
        return value?.value
    }
}
