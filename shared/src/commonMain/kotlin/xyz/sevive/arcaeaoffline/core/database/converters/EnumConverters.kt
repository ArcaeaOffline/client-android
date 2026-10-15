package xyz.sevive.arcaeaoffline.core.database.converters

import androidx.room3.ColumnTypeConverter
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaLanguage
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass

object ArcaeaRatingClassConverters {
    @ColumnTypeConverter
    fun fromDatabaseValue(value: Int?): ArcaeaRatingClass? = value?.let { ArcaeaRatingClass.fromInt(it) }

    @ColumnTypeConverter
    fun toDatabaseValue(value: ArcaeaRatingClass?): Int? = value?.value
}

object ArcaeaPlayResultClearTypeConverters {
    @ColumnTypeConverter
    fun fromDatabaseValue(value: Int?): ArcaeaPlayResultClearType? = value?.let { ArcaeaPlayResultClearType.fromInt(it) }

    @ColumnTypeConverter
    fun toDatabaseValue(value: ArcaeaPlayResultClearType?): Int? = value?.value
}

object ArcaeaPlayResultModifierConverters {
    @ColumnTypeConverter
    fun fromDatabaseValue(value: Int?): ArcaeaPlayResultModifier? = value?.let { ArcaeaPlayResultModifier.fromInt(it) }

    @ColumnTypeConverter
    fun toDatabaseValue(value: ArcaeaPlayResultModifier?): Int? = value?.value
}

object ArcaeaLanguageConverters {
    @ColumnTypeConverter
    fun fromDatabaseValue(value: String?): ArcaeaLanguage? = value?.let { ArcaeaLanguage.entries.find { it.code == value } }

    @ColumnTypeConverter
    fun toDatabaseValue(value: ArcaeaLanguage?): String? = value?.code
}
