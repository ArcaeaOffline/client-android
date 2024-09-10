package xyz.sevive.arcaeaoffline.database.converters

import androidx.room.TypeConverter
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning

object ArcaeaPlayResultValidatorWarningsConverters {
    @TypeConverter
    fun fromDatabaseValue(value: String?): List<ArcaeaPlayResultValidatorWarning>? {
        return value?.split(",")
            ?.mapNotNull { ArcaeaPlayResultValidator.WARNINGS.find { w -> w.id == it } }
    }

    @TypeConverter
    fun toDatabaseValue(value: List<ArcaeaPlayResultValidatorWarning>?): String? {
        return when {
            value == null -> null
            value.isEmpty() -> null
            else -> value.joinToString(",") { it.id }
        }
    }
}
