package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass

@DatabaseView(
    """
        SELECT
            sc.id, sc.song_id, sc.rating_class, sc.score,
            sc.pure, sc.shiny_pure, sc.far, sc.lost,
            sc.date, sc.max_recall, sc.modifier, sc.clear_type,
            MAX(sc.potential) AS potential,
            sc.comment
        FROM scores_calculated sc
        GROUP BY sc.song_id, sc.rating_class
        ORDER BY sc.potential DESC
    """, "scores_best"
)
data class PlayResultBest(
    val id: Int,
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaRatingClass,
    val score: Int,
    val pure: Int?,
    @ColumnInfo(name = "shiny_pure") val shinyPure: Int?,
    val far: Int?,
    val lost: Int?,
    val date: Instant?,
    @ColumnInfo(name = "max_recall") val maxRecall: Int?,
    val modifier: ArcaeaPlayResultModifier?,
    @ColumnInfo(name = "clear_type") val clearType: ArcaeaPlayResultClearType?,
    val potential: Double,
    val comment: String?,
)

fun PlayResultBest.toPlayResult(): PlayResult {
    return PlayResult(
        id = this.id,
        songId = this.songId,
        ratingClass = this.ratingClass,
        score = this.score,
        pure = this.pure,
        far = this.far,
        lost = this.lost,
        date = this.date,
        maxRecall = this.maxRecall,
        modifier = this.modifier,
        clearType = this.clearType,
        comment = this.comment,
    )
}
