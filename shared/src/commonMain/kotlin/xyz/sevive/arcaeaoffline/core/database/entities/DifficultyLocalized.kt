package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaLanguage
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass

@Entity(
    tableName = "difficulties_localized",
    primaryKeys = ["song_id", "rating_class"],
    indices = [
        Index(value = ["song_id", "rating_class", "lang"], unique = true),
        Index(value = ["lang"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Difficulty::class,
            parentColumns = ["song_id", "rating_class"],
            childColumns = ["song_id", "rating_class"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
            deferred = true,
        ),
    ],
)
data class DifficultyLocalized(
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaRatingClass,
    val lang: ArcaeaLanguage,
    val title: String?,
    val artist: String?,
)
