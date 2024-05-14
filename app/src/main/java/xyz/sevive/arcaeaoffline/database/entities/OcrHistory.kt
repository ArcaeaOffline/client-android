package xyz.sevive.arcaeaoffline.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Score


@Entity(tableName = "ocr_history")
data class OcrHistory(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "source_package_name") val sourcePackageName: String?,
    @ColumnInfo(name = "store_date") val storeDate: Long,

    @ColumnInfo(name = "song_id") val songId: String?,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaScoreRatingClass,
    @ColumnInfo(name = "score") val score: Int,
    @ColumnInfo(name = "pure") val pure: Int?,
    @ColumnInfo(name = "far") val far: Int?,
    @ColumnInfo(name = "lost") val lost: Int?,
    @ColumnInfo(name = "date") val date: Long?,
    @ColumnInfo(name = "max_recall") val maxRecall: Int?,
    @ColumnInfo(name = "modifier") val modifier: ArcaeaScoreModifier?,
    @ColumnInfo(name = "clear_type") val clearType: ArcaeaScoreClearType?,
) {
    fun toArcaeaScore(comment: String? = ""): Score? {
        if (songId == null) return null

        return Score(
            id = 0,
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
                    stringArr.add(
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(
                            LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(storeDate), ZoneId.systemDefault()
                            )
                        )
                    )

                    stringArr.joinToString(", ")
                }

                else -> comment
            },
        )
    }

    companion object {
        fun fromArcaeaScore(
            score: Score,
            sourcePackageName: String? = null,
            storeDate: Long = Instant.now().epochSecond,
        ): OcrHistory {
            return OcrHistory(
                id = 0,
                sourcePackageName = sourcePackageName,
                storeDate = storeDate,
                songId = score.songId,
                ratingClass = score.ratingClass,
                score = score.score,
                pure = score.pure,
                far = score.far,
                lost = score.lost,
                date = score.date,
                maxRecall = score.maxRecall,
                modifier = score.modifier,
                clearType = score.clearType
            )
        }
    }
}
