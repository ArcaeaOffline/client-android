package xyz.sevive.arcaeaoffline.core.database.converters

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant

object InstantConverters {
    @ColumnTypeConverter
    fun fromTimestamp(value: Long?): Instant? = value?.let { Instant.fromEpochMilliseconds(it) }

    @ColumnTypeConverter
    fun dateToTimestamp(date: Instant?): Long? = date?.toEpochMilliseconds()
}
