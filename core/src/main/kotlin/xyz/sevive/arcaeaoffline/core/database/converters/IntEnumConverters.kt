package xyz.sevive.arcaeaoffline.core.database.converters

import androidx.room.TypeConverter
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass


object ArcaeaRatingClassConverters {
    @TypeConverter
    fun fromDatabaseValue(value: Int?): ArcaeaRatingClass? {
        return value?.let { ArcaeaRatingClass.fromInt(it) }
    }

    @TypeConverter
    fun toDatabaseValue(value: ArcaeaRatingClass?): Int? {
        return value?.value
    }
}

object ArcaeaPlayResultClearTypeConverters {
    @TypeConverter
    fun fromDatabaseValue(value: Int?): ArcaeaPlayResultClearType? {
        return value?.let { ArcaeaPlayResultClearType.fromInt(it) }
    }

    @TypeConverter
    fun toDatabaseValue(value: ArcaeaPlayResultClearType?): Int? {
        return value?.value
    }
}


object ArcaeaPlayResultModifierConverters {
    @TypeConverter
    fun fromDatabaseValue(value: Int?): ArcaeaPlayResultModifier? {
        return value?.let { ArcaeaPlayResultModifier.fromInt(it) }
    }

    @TypeConverter
    fun toDatabaseValue(value: ArcaeaPlayResultModifier?): Int? {
        return value?.value
    }
}
