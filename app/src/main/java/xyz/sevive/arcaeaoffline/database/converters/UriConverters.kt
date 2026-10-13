package xyz.sevive.arcaeaoffline.database.converters

import android.net.Uri
import androidx.core.net.toUri
import androidx.room3.ColumnTypeConverter

object UriConverters {
    @ColumnTypeConverter
    fun fromDatabaseValue(value: String?): Uri? = value?.toUri()

    @ColumnTypeConverter
    fun toDatabaseValue(value: Uri?): String? = value?.toString()
}
