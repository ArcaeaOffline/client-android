package xyz.sevive.arcaeaoffline.core.database.converters

import androidx.room.TypeConverter
import java.time.Instant

object InstantConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? = date?.toEpochMilli()
}
