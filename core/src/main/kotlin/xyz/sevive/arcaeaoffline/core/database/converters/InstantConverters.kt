package xyz.sevive.arcaeaoffline.core.database.converters

import androidx.room.TypeConverter
import kotlin.time.Instant

object InstantConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? = date?.toEpochMilliseconds()
}
