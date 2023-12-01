package xyz.sevive.arcaeaoffline.database.entities

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.database.entities.Score


@Entity(tableName = "ocr_history")
data class OcrHistory(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "source_package_name") val sourcePackageName: String? = "",
    @ColumnInfo(name = "store_date") val storeDate: Long,

    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: Int,
    @ColumnInfo(name = "score") val score: Int,
    @ColumnInfo(name = "pure") val pure: Int?,
    @ColumnInfo(name = "far") val far: Int?,
    @ColumnInfo(name = "lost") val lost: Int?,
    @ColumnInfo(name = "date") val date: Long?,
    @ColumnInfo(name = "max_recall") val maxRecall: Int?,
    @ColumnInfo(name = "modifier") val modifier: Int?,
    @ColumnInfo(name = "clear_type") val clearType: Int?,
) {
    fun toArcaeaScore(comment: String? = ""): Score {
        return Score(
            id = 0,
            songId = songId,
            ratingClass = ratingClass,
            score = score,
            pure = pure,
            far = far,
            lost = lost,
            date = date?.toInt(),
            maxRecall = maxRecall,
            modifier = modifier,
            clearType = clearType,
            comment = when (comment) {
                null -> null
                "" -> "Restored from OCR history, id = $id, source = $sourcePackageName, timestamp = $storeDate"
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
                date = score.date?.toLong(),
                maxRecall = score.maxRecall,
                modifier = score.modifier,
                clearType = score.clearType
            )
        }
    }
}

@Dao
interface OcrHistoryDao {
    @Query("SELECT * FROM ocr_history")
    fun findAll(): Flow<List<OcrHistory>>

    @Query("SELECT * FROM ocr_history WHERE id IN (:ids)")
    fun findAllByIds(ids: IntArray): Flow<List<OcrHistory>>

    @Query("SELECT * FROM ocr_history WHERE id = (:id)")
    fun findById(id: Int): Flow<OcrHistory>

    @Insert
    suspend fun insert(item: OcrHistory)

    @Insert
    suspend fun insertAll(vararg items: OcrHistory)

    @Delete
    suspend fun delete(item: OcrHistory)
}

