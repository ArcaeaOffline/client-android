package xyz.sevive.arcaeaoffline.core.database.externals.arcaea.importers

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


object ArcaeaSt3PlayResultImporter {
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
        cursor.use {
            do {
                val songId = it.getString(it.getColumnIndexOrThrow("songId"))
                val ratingClass = it.getInt(it.getColumnIndexOrThrow("ratingClass"))
                val score = it.getInt(it.getColumnIndexOrThrow("score"))
                val pure = it.getIntOrNull(it.getColumnIndex("pure"))
                val far = it.getIntOrNull(it.getColumnIndex("far"))
                val lost = it.getIntOrNull(it.getColumnIndex("lost"))
                val date = fixTimestamp(it.getLongOrNull(it.getColumnIndex("date")))
                val modifier = it.getIntOrNull(it.getColumnIndex("modifier"))
                val clearType = it.getIntOrNull(it.getColumnIndex("clearType"))

                val commentDateString = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

                items.add(
                    PlayResult(
                        songId = songId,
                        ratingClass = ArcaeaRatingClass.fromInt(ratingClass),
                        score = score,
                        pure = pure,
                        far = far,
                        lost = lost,
                        date = date?.let { Instant.ofEpochSecond(date) },
                        modifier = modifier?.let { ArcaeaPlayResultModifier.fromInt(modifier) },
                        clearType = clearType?.let { ArcaeaPlayResultClearType.fromInt(clearType) },
                        comment = "Imported from st3 at $commentDateString"
                    )
                )
            } while (it.moveToNext())
        }

        return items
    }
}
