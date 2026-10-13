package xyz.sevive.arcaeaoffline.database.converters

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult

object DeviceOcrResultConverters {
    @ColumnTypeConverter
    fun fromDatabaseValue(value: String?): DeviceOcrResult? = value?.let { Json.decodeFromString<DeviceOcrResult>(it) }

    @ColumnTypeConverter
    fun toDatabaseValue(value: DeviceOcrResult?): String? = value?.let { Json.encodeToString(DeviceOcrResult.serializer(), it) }
}
