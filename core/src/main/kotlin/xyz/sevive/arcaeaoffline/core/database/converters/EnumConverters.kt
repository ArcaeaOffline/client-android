package xyz.sevive.arcaeaoffline.core.database.converters

import androidx.room.TypeConverter
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaLanguage
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass

object ArcaeaRatingClassConverters {
    @TypeConverter
    fun fromDatabaseValue(value: Int?): ArcaeaRatingClass? = value?.let { ArcaeaRatingClass.fromInt(it) }

    @TypeConverter
    fun toDatabaseValue(value: ArcaeaRatingClass?): Int? = value?.value
}

object ArcaeaPlayResultClearTypeConverters {
    @TypeConverter
    fun fromDatabaseValue(value: Int?): ArcaeaPlayResultClearType? = value?.let { ArcaeaPlayResultClearType.fromInt(it) }

    @TypeConverter
    fun toDatabaseValue(value: ArcaeaPlayResultClearType?): Int? = value?.value
}

object ArcaeaPlayResultModifierConverters {
    @TypeConverter
    fun fromDatabaseValue(value: Int?): ArcaeaPlayResultModifier? = value?.let { ArcaeaPlayResultModifier.fromInt(it) }

    @TypeConverter
    fun toDatabaseValue(value: ArcaeaPlayResultModifier?): Int? = value?.value
}

object ArcaeaLanguageConverters {
    @TypeConverter
    fun fromDatabaseValue(value: String?): ArcaeaLanguage? = value?.let { ArcaeaLanguage.entries.find { it.code == value } }

    @TypeConverter
    fun toDatabaseValue(value: ArcaeaLanguage?): String? = value?.code
}
