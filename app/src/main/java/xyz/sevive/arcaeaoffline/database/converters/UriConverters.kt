package xyz.sevive.arcaeaoffline.database.converters

import android.net.Uri
import androidx.room.TypeConverter


object UriConverters {
    @TypeConverter
    fun fromDatabaseValue(value: String?): Uri? {
        return value?.let { Uri.parse(it) }
    }

    @TypeConverter
    fun toDatabaseValue(value: Uri?): String? {
        return value?.toString()
    }
}
