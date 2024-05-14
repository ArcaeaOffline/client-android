package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass

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
data class ScoreBest(
    val id: Int,
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaScoreRatingClass,
    val score: Int,
    val pure: Int?,
    @ColumnInfo(name = "shiny_pure") val shinyPure: Int?,
    val far: Int?,
    val lost: Int?,
    val date: Long?,
    @ColumnInfo(name = "max_recall") val maxRecall: Int?,
    val modifier: ArcaeaScoreModifier?,
    @ColumnInfo(name = "clear_type") val clearType: ArcaeaScoreClearType?,
    val potential: Double,
    val comment: String?,
)

fun ScoreBest.toScore(): Score {
    return Score(
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
