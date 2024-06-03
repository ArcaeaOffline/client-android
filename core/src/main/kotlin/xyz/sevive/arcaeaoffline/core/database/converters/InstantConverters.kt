package xyz.sevive.arcaeaoffline.core.database.converters

import androidx.room.TypeConverter
import org.threeten.bp.Instant

object InstantConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }
}
