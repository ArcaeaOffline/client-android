package xyz.sevive.arcaeaoffline.core.database.externals.importers

import android.database.Cursor
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import io.requery.android.database.sqlite.SQLiteDatabase
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

/**
 * Some of the `date` column in st3 are unexpectedly truncated. For example,
 * a `1670283375` may be truncated to `167028`, even a single `1`.
 *
 * To properly handle this:
 *
 * * If `timestamp > 1489017600` (the release date of Arcaea), consider it's ok.
 *
 * * Otherwise, if the timestamp is *fixable*
 * (`1489 <= timestamp <= 9999` or `timestamp > 14889`),
 * pad zeros to the end of timestamp.
 * For example, a `1566` will be padded to `1566000000`.
 *
 * * Otherwise, treat the timestamp as `None`.
 *
 * @param [ts] The `date` value from st3
 */
private fun fixTimestamp(ts: Long?): Long? {
    if (ts == null || ts > 1489017600) return ts

    val isFixable = (ts in 1489..9999) || ts > 14889
    if (!isFixable) return null

    return ts.toString().padEnd(10, '0').toLong()
}

private data class St3PlayResult(
    val songId: String,
    val ratingClass: Int,
    val score: Int,
    val pure: Int?,
    val far: Int?,
    val lost: Int?,
    val date: Long?,
    val modifier: Int?,
    val clearType: Int?,
) {
    constructor(cursor: Cursor) : this(
        songId = cursor.getString(cursor.getColumnIndexOrThrow("songId")),
        ratingClass = cursor.getInt(cursor.getColumnIndexOrThrow("ratingClass")),
        score = cursor.getInt(cursor.getColumnIndexOrThrow("score")),
        pure = cursor.getIntOrNull(cursor.getColumnIndex("pure")),
        far = cursor.getIntOrNull(cursor.getColumnIndex("far")),
        lost = cursor.getIntOrNull(cursor.getColumnIndex("lost")),
        date = fixTimestamp(cursor.getLongOrNull(cursor.getColumnIndex("date"))),
        modifier = cursor.getIntOrNull(cursor.getColumnIndex("modifier")),
        clearType = cursor.getIntOrNull(cursor.getColumnIndex("clearType")),
    )

    val clearTypeReliable: Boolean
        get() {
            if (clearType == ArcaeaPlayResultClearType.FULL_RECALL.value && lost != 0) {
                return false
            }
            if (clearType == ArcaeaPlayResultClearType.PURE_MEMORY.value && lost != 0 && far != 0) {
                return false
            }

            return true
        }

    fun toPlayResult(importDate: LocalDate? = null): PlayResult {
        val commentDateString = (importDate ?: LocalDate.now()).format(DateTimeFormatter.ISO_DATE)

        return PlayResult(
            songId = songId,
            ratingClass = ArcaeaRatingClass.fromInt(ratingClass),
            score = score,
            pure = pure,
            far = far,
            lost = lost,
            date = date?.let { Instant.ofEpochSecond(date) },
            modifier = modifier?.let { ArcaeaPlayResultModifier.fromInt(modifier) },
            clearType = if (clearTypeReliable) clearType?.let {
                ArcaeaPlayResultClearType.fromInt(it)
            } else null,
            comment = "Imported from st3 at $commentDateString",
        )
    }
}

object ArcaeaSt3PlayResultImporter {
    fun playResults(db: SQLiteDatabase): List<PlayResult> {
        val items = mutableListOf<PlayResult>()

        val cursor = db.rawQuery(
            """
                SELECT s.songId, s.songDifficulty AS ratingClass, s.score,
                       s.perfectCount AS pure, s.nearCount AS far, s.missCount AS lost,
                       s.`date`, s.modifier, ct.clearType  
                FROM scores s JOIN cleartypes ct
                ON s.songId = ct.songId AND s.songDifficulty = ct.songDifficulty
            """, emptyArray()
        )

        if (!cursor.moveToFirst()) return emptyList()

        val importDate = LocalDate.now()
        cursor.use {
            do {
                val st3Item = St3PlayResult(it)
                var playResult = st3Item.toPlayResult(importDate)

                if (playResult.clearType == ArcaeaPlayResultClearType.FULL_RECALL) {
                    playResult = playResult.copy(
                        maxRecall = playResult.pure!! + playResult.far!!
                    )
                }

                if (playResult.clearType == ArcaeaPlayResultClearType.PURE_MEMORY) {
                    playResult = playResult.copy(maxRecall = playResult.pure)
                }

                items.add(playResult)
            } while (it.moveToNext())
        }

        return items
    }
}
