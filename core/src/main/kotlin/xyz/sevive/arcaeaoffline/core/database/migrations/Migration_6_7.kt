package xyz.sevive.arcaeaoffline.core.database.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.room.migration.Migration
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import xyz.sevive.arcaeaoffline.core.database.converters.UUIDByteArrayConverters
import java.util.UUID


private const val CREATE_TABLE_PLAY_RESULTS_7 =
    "CREATE TABLE play_results (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uuid` BLOB NOT NULL, `song_id` TEXT NOT NULL, `rating_class` TEXT NOT NULL, `score` INTEGER NOT NULL, `pure` INTEGER, `far` INTEGER, `lost` INTEGER, `date` INTEGER, `max_recall` INTEGER, `modifier` TEXT, `clear_type` TEXT, `comment` TEXT)"
private const val CREATE_UNIQUE_INDEX_PLAY_RESULTS_UUID_7 =
    "CREATE UNIQUE INDEX IF NOT EXISTS `index_play_results_uuid` ON play_results (`uuid`)"
private const val CREATE_VIEW_PLAY_RESULTS_CALCULATED_7 =
    "CREATE VIEW `play_results_calculated` AS SELECT\n        pr.id, pr.uuid, d.song_id, d.rating_class, pr.score, pr.pure,\n        CASE\n            WHEN ci.notes IS NOT NULL AND pr.pure IS NOT NULL AND pr.far IS NOT NULL AND ci.notes <> 0\n            THEN pr.score - FLOOR((pr.pure * 10000000.0 / ci.notes) + (pr.far * 0.5 * 100000000.0 / ci.notes))\n            ELSE NULL\n        END AS shiny_pure,\n        pr.far, pr.lost, pr.date, pr.max_recall, pr.modifier, pr.clear_type,\n        CASE\n            WHEN pr.score >= 100000000 THEN ci.constant / 10.0 + 2\n            WHEN pr.score >= 9800000 THEN ci.constant / 10.0 + 1 + (pr.score - 9800000) / 200000.0\n            ELSE MAX(ci.constant / 10.0 + (pr.score - 9500000) / 300000.0, 0)\n        END AS potential,\n        pr.comment\n    FROM difficulties d\n    JOIN charts_info ci ON d.song_id = ci.song_id AND d.rating_class = ci.rating_class\n    JOIN play_results pr ON d.song_id = pr.song_id AND d.rating_class = pr.rating_class"
private const val CREATE_VIEW_PLAY_RESULTS_BEST_7 =
    "CREATE VIEW `play_results_best` AS SELECT\n            prc.id, prc.uuid, prc.song_id, prc.rating_class, prc.score,\n            prc.pure, prc.shiny_pure, prc.far, prc.lost,\n            prc.date, prc.max_recall, prc.modifier, prc.clear_type,\n            MAX(prc.potential) AS potential,\n            prc.comment\n        FROM play_results_calculated prc\n        GROUP BY prc.song_id, prc.rating_class\n        ORDER BY prc.potential DESC"

object Migration_6_7 : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val playResultsContentValues = mutableListOf<ContentValues>()

        val query = SimpleSQLiteQuery(
            "SELECT id, song_id, rating_class, score, pure, far, lost, date, max_recall, modifier, clear_type, comment FROM play_results"
        )

        db.query(query).use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val songId = it.getString(it.getColumnIndexOrThrow("song_id"))
                val ratingClass = it.getString(it.getColumnIndexOrThrow("rating_class"))
                val score = it.getInt(it.getColumnIndexOrThrow("score"))
                val pure = it.getIntOrNull(it.getColumnIndex("pure"))
                val far = it.getIntOrNull(it.getColumnIndex("far"))
                val lost = it.getIntOrNull(it.getColumnIndex("lost"))
                val date = it.getLongOrNull(it.getColumnIndex("date"))
                val maxRecall = it.getIntOrNull(it.getColumnIndex("max_recall"))
                val modifier = it.getStringOrNull(it.getColumnIndex("modifier"))
                val clearType = it.getStringOrNull(it.getColumnIndex("clear_type"))
                val comment = it.getStringOrNull(it.getColumnIndex("comment"))

                ContentValues().apply {
                    put("id", id)

                    // the only real logic bro
                    put("uuid", UUIDByteArrayConverters.toDatabaseValue(UUID.randomUUID()))

                    put("song_id", songId)
                    put("rating_class", ratingClass)
                    put("score", score)
                    if (pure != null) put("pure", pure)
                    if (far != null) put("far", far)
                    if (lost != null) put("lost", lost)
                    if (date != null) put("date", date)
                    if (maxRecall != null) put("max_recall", maxRecall)
                    if (modifier != null) put("modifier", modifier)
                    if (clearType != null) put("clear_type", clearType)
                    if (comment != null) put("comment", comment)

                    playResultsContentValues.add(this)
                }
            }
        }

        db.execSQL("DROP TABLE play_results")
        db.execSQL("DROP VIEW play_results_calculated")
        db.execSQL("DROP VIEW play_results_best")

        db.execSQL(CREATE_TABLE_PLAY_RESULTS_7)
        db.execSQL(CREATE_UNIQUE_INDEX_PLAY_RESULTS_UUID_7)
        db.execSQL(CREATE_VIEW_PLAY_RESULTS_CALCULATED_7)
        db.execSQL(CREATE_VIEW_PLAY_RESULTS_BEST_7)

        playResultsContentValues.forEach {
            db.insert("play_results", SQLiteDatabase.CONFLICT_FAIL, it)
        }
    }
}
