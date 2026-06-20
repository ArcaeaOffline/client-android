package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.extensions.PlayResultSerializer
import java.util.UUID
import kotlin.time.Instant

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
    val uuid: UUID = UUID.randomUUID(),
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

fun PlayResult.potential(constant: Int): Double = calculatePotential(this.score, constant)

fun PlayResult.potential(chartInfo: ChartInfo) = potential(chartInfo.constant)

fun PlayResult.potential(chart: Chart) = potential(chart.constant)
