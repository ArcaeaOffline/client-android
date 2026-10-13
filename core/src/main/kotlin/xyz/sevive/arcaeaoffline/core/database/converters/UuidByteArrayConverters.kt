package xyz.sevive.arcaeaoffline.core.database.converters

import androidx.room3.ColumnTypeConverter
import kotlin.uuid.Uuid

object UuidByteArrayConverters {
    @ColumnTypeConverter
    fun fromDatabaseValue(value: ByteArray?): Uuid? = value?.let { Uuid.fromByteArray(it) }

    @ColumnTypeConverter
    fun toDatabaseValue(uuid: Uuid?): ByteArray? = uuid?.toByteArray()
}
