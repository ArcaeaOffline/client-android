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
        s.id, d.song_id, d.rating_class, s.score, s.pure,
        CASE
            WHEN ci.notes IS NOT NULL AND s.pure IS NOT NULL AND s.far IS NOT NULL AND ci.notes <> 0
            THEN s.score - FLOOR((s.pure * 10000000.0 / ci.notes) + (s.far * 0.5 * 100000000.0 / ci.notes))
            ELSE NULL
        END AS shiny_pure,
        s.far, s.lost, s.date, s.max_recall, s.modifier, s.clear_type,
        CASE
            WHEN s.score >= 100000000 THEN ci.constant / 10.0 + 2
            WHEN s.score >= 9800000 THEN ci.constant / 10.0 + 1 + (s.score - 9800000) / 200000.0
            ELSE MAX(ci.constant / 10.0 + (s.score - 9500000) / 300000.0, 0)
        END AS potential,
        s.comment
    FROM difficulties d
    JOIN charts_info ci ON d.song_id = ci.song_id AND d.rating_class = ci.rating_class
    JOIN scores s ON d.song_id = s.song_id AND d.rating_class = s.rating_class
""", "scores_calculated"
)
data class ScoreCalculated(
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
