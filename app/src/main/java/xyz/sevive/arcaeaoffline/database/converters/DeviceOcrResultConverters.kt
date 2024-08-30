package xyz.sevive.arcaeaoffline.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult


object DeviceOcrResultConverters {
    @TypeConverter
    fun fromDatabaseValue(value: String?): DeviceOcrResult? {
        return value?.let { Json.decodeFromString<DeviceOcrResult>(it) }
    }

    @TypeConverter
    fun toDatabaseValue(value: DeviceOcrResult?): String? {
        return value?.let { Json.encodeToString(DeviceOcrResult.serializer(), it) }
    }
}
