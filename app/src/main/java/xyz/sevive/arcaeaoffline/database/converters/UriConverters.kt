package xyz.sevive.arcaeaoffline.database.converters

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter

object UriConverters {
    @TypeConverter
    fun fromDatabaseValue(value: String?): Uri? = value?.toUri()

    @TypeConverter
    fun toDatabaseValue(value: Uri?): String? = value?.toString()
}
