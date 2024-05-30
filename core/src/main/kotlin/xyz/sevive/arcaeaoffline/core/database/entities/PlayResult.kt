package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass


@Entity(tableName = "scores")
data class PlayResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaRatingClass,
    val score: Int,
    val pure: Int? = null,
    val far: Int? = null,
    val lost: Int? = null,
    val date: Instant? = null,
    @ColumnInfo(name = "max_recall") val maxRecall: Int? = null,
    val modifier: ArcaeaPlayResultModifier? = null,
    @ColumnInfo(name = "clear_type") val clearType: ArcaeaPlayResultClearType? = null,
    val comment: String? = null,
)
