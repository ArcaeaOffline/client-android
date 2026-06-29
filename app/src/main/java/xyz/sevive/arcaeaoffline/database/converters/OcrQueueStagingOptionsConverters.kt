package xyz.sevive.arcaeaoffline.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingOptions

object OcrQueueStagingOptionsConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromDatabaseValue(value: String?): OcrQueueStagingOptions =
        try {
            value?.let { json.decodeFromString(it) } ?: OcrQueueStagingOptions.DEFAULTS
        } catch (_: SerializationException) {
            OcrQueueStagingOptions.DEFAULTS
        }

    @TypeConverter
    fun toDatabaseValue(value: OcrQueueStagingOptions): String = json.encodeToString(value)
}
