package xyz.sevive.arcaeaoffline.core.database.converters

import androidx.room.TypeConverter
import kotlin.uuid.Uuid

object UuidByteArrayConverters {
    @TypeConverter
    fun fromDatabaseValue(value: ByteArray?): Uuid? = value?.let { Uuid.fromByteArray(it) }

    @TypeConverter
    fun toDatabaseValue(uuid: Uuid?): ByteArray? = uuid?.toByteArray()
}
