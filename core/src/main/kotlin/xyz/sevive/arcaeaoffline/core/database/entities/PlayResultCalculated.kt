package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import java.util.UUID

@DatabaseView(
    """
    SELECT
        pr.id, pr.uuid, d.song_id, d.rating_class, pr.score, pr.pure,
        CASE
            WHEN ci.notes IS NOT NULL AND pr.pure IS NOT NULL AND pr.far IS NOT NULL AND ci.notes <> 0
            THEN pr.score - FLOOR((pr.pure * 10000000.0 / ci.notes) + (pr.far * 0.5 * 100000000.0 / ci.notes))
            ELSE NULL
        END AS shiny_pure,
        pr.far, pr.lost, pr.date, pr.max_recall, pr.modifier, pr.clear_type,
        CASE
            WHEN pr.score >= 100000000 THEN ci.constant / 10.0 + 2
            WHEN pr.score >= 9800000 THEN ci.constant / 10.0 + 1 + (pr.score - 9800000) / 200000.0
            ELSE MAX(ci.constant / 10.0 + (pr.score - 9500000) / 300000.0, 0)
        END AS potential,
        pr.comment
    FROM difficulties d
    JOIN charts_info ci ON d.song_id = ci.song_id AND d.rating_class = ci.rating_class
    JOIN play_results pr ON d.song_id = pr.song_id AND d.rating_class = pr.rating_class
""", "play_results_calculated"
)
data class PlayResultCalculated(
    val id: Long,
    val uuid: UUID,
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
