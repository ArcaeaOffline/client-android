package xyz.sevive.arcaeaoffline.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDateTime
import java.util.UUID


@Entity(tableName = "ocr_history")
data class OcrHistory(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "source_package_name") val sourcePackageName: String?,
    @ColumnInfo(name = "store_date") val storeDate: Long,

    @ColumnInfo(name = "song_id") val songId: String?,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaRatingClass,
    @ColumnInfo(name = "playResult") val score: Int,
    @ColumnInfo(name = "pure") val pure: Int?,
    @ColumnInfo(name = "far") val far: Int?,
    @ColumnInfo(name = "lost") val lost: Int?,
    @ColumnInfo(name = "date") val date: Instant?,
    @ColumnInfo(name = "max_recall") val maxRecall: Int?,
    @ColumnInfo(name = "modifier") val modifier: ArcaeaPlayResultModifier?,
    @ColumnInfo(name = "clear_type") val clearType: ArcaeaPlayResultClearType?,
) {
    fun toArcaeaScore(comment: String? = ""): PlayResult? {
        if (songId == null) return null

        return PlayResult(
            id = 0,
            uuid = UUID.randomUUID(),
            songId = songId,
            ratingClass = ratingClass,
            score = score,
            pure = pure,
            far = far,
            lost = lost,
            date = date,
            maxRecall = maxRecall,
            modifier = modifier,
            clearType = clearType,
            comment = when (comment) {
                null -> null
                "" -> {
                    val stringArr = mutableListOf("From OCR cache")
                    if (sourcePackageName != null) stringArr.add("source `$sourcePackageName`")
                    stringArr.add(Instant.ofEpochSecond(storeDate).formatAsLocalizedDateTime())

                    stringArr.joinToString(", ")
                }

                else -> comment
            },
        )
    }

    companion object {
        fun fromArcaeaScore(
            playResult: PlayResult,
            sourcePackageName: String? = null,
            storeDate: Long = Instant.now().epochSecond,
        ): OcrHistory {
            return OcrHistory(
                id = 0,
                sourcePackageName = sourcePackageName,
                storeDate = storeDate,
                songId = playResult.songId,
                ratingClass = playResult.ratingClass,
                score = playResult.score,
                pure = playResult.pure,
                far = playResult.far,
                lost = playResult.lost,
                date = playResult.date,
                maxRecall = playResult.maxRecall,
                modifier = playResult.modifier,
                clearType = playResult.clearType
            )
        }
    }
}
