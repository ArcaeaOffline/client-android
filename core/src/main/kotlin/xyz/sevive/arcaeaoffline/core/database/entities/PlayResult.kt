package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import xyz.sevive.arcaeaoffline.core.calculators.calculatePlayRating
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.extensions.PlayResultSerializer
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable(with = PlayResultSerializer::class)
@Entity(
    tableName = "play_results",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["song_id", "rating_class"]),
    ],
)
data class PlayResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: Uuid = Uuid.generateV4(),
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

fun PlayResult.playRating(constant: Int): Double = calculatePlayRating(this.score, constant)

fun PlayResult.playRating(chartInfo: ChartInfo) = playRating(chartInfo.constant)

fun PlayResult.playRating(chart: Chart) = playRating(chart.constant)
