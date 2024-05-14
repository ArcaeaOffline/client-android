package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass


@Entity(
    tableName = "difficulties_localized",
    primaryKeys = ["song_id", "rating_class"],
    foreignKeys = [ForeignKey(
        entity = Difficulty::class,
        parentColumns = ["song_id", "rating_class"],
        childColumns = ["song_id", "rating_class"],
        onUpdate = ForeignKey.NO_ACTION,
        deferred = true
    )]
)
data class DifficultyLocalized(
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaScoreRatingClass,
    @ColumnInfo(name = "title_ja") val titleJa: String?,
    @ColumnInfo(name = "title_ko") val titleKo: String?,
    @ColumnInfo(name = "title_zh_hans") val titleZhHans: String?,
    @ColumnInfo(name = "title_zh_hant") val titleZhHant: String?,
    @ColumnInfo(name = "artist_ja") val artistJa: String?,
    @ColumnInfo(name = "artist_ko") val artistKo: String?,
    @ColumnInfo(name = "artist_zh_hans") val artistZhHans: String?,
    @ColumnInfo(name = "artist_zh_hant") val artistZhHant: String?,
)
