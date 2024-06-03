package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import kotlin.math.max


@Entity(tableName = "play_results")
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

fun PlayResult.potential(constant: Int): Double {
    return if (score >= 100000000) {
        constant / 10.0 + 2
    } else if (score >= 9800000) {
        constant / 10.0 + 1 + (score - 9800000) / 200000.0
    } else {
        max(0.0, constant / 10.0 + (score - 9500000) / 300000.0)
    }
}

fun PlayResult.potential(chartInfo: ChartInfo) = potential(chartInfo.constant)
fun PlayResult.potential(chart: Chart) = potential(chart.constant)
