package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass


@Entity(
    tableName = "difficulties_localized",
    primaryKeys = ["song_id", "rating_class"],
    indices = [
        Index(value = ["song_id", "rating_class", "lang"], unique = true),
        Index(value = ["lang"]),
    ],
    foreignKeys = [ForeignKey(
        entity = Difficulty::class,
        parentColumns = ["song_id", "rating_class"],
        childColumns = ["song_id", "rating_class"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE,
        deferred = true
    )],
)
data class DifficultyLocalized(
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaRatingClass,
    val lang: String,
    val title: String?,
    val artist: String?,
)
