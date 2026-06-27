package xyz.sevive.arcaeaoffline.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueOptions

object OcrQueueEnqueueOptionsConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromDatabaseValue(value: String?): OcrQueueEnqueueOptions =
        try {
            value?.let { json.decodeFromString(it) } ?: OcrQueueEnqueueOptions()
        } catch (_: Exception) {
            OcrQueueEnqueueOptions()
        }

    @TypeConverter
    fun toDatabaseValue(value: OcrQueueEnqueueOptions): String = json.encodeToString(value)
}
