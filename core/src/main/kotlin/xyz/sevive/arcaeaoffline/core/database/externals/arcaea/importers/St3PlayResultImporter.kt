package xyz.sevive.arcaeaoffline.core.database.externals.arcaea.importers

import io.requery.android.database.sqlite.SQLiteDatabase
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.migrations.getIntOrNull
import xyz.sevive.arcaeaoffline.core.database.migrations.getLongOrNull


object St3PlayResultImporter {
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

        val songIdIdx = cursor.getColumnIndexOrThrow("songId")
        val ratingClassIdx = cursor.getColumnIndexOrThrow("ratingClass")
        val scoreIdx = cursor.getColumnIndexOrThrow("score")

        if (!cursor.moveToFirst()) return emptyList()

        do {
            val songId = cursor.getString(songIdIdx)
            val ratingClass = cursor.getInt(ratingClassIdx)
            val score = cursor.getInt(scoreIdx)
            val pure = cursor.getIntOrNull("pure")
            val far = cursor.getIntOrNull("far")
            val lost = cursor.getIntOrNull("lost")
            val date = fixTimestamp(cursor.getLongOrNull("date"))
            val modifier = cursor.getIntOrNull("modifier")
            val clearType = cursor.getIntOrNull("clearType")

            val commentDateString = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

            items.add(
                PlayResult(
                    songId = songId,
                    ratingClass = ArcaeaRatingClass.fromInt(ratingClass),
                    score = score,
                    pure = pure,
                    far = far,
                    lost = lost,
                    date = date?.let { Instant.ofEpochSecond(it) },
                    modifier = modifier?.let { ArcaeaPlayResultModifier.fromInt(it) },
                    clearType = clearType?.let { ArcaeaPlayResultClearType.fromInt(it) },
                    comment = "Imported from st3 at $commentDateString"
                )
            )
        } while (cursor.moveToNext())

        return items
    }
}
