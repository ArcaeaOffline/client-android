package xyz.sevive.arcaeaoffline.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import xyz.sevive.arcaeaoffline.core.database.extensions.bindIntOrNull
import xyz.sevive.arcaeaoffline.core.database.extensions.bindLongOrNull
import xyz.sevive.arcaeaoffline.core.database.extensions.bindTextOrNull
import xyz.sevive.arcaeaoffline.core.database.extensions.getIntOrNull
import xyz.sevive.arcaeaoffline.core.database.extensions.getLongOrNull
import xyz.sevive.arcaeaoffline.core.database.extensions.getTextOrNull
import java.nio.ByteBuffer
import java.util.UUID


private const val CREATE_TABLE_PLAY_RESULTS_7 =
    "CREATE TABLE play_results (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uuid` BLOB NOT NULL, `song_id` TEXT NOT NULL, `rating_class` TEXT NOT NULL, `score` INTEGER NOT NULL, `pure` INTEGER, `far` INTEGER, `lost` INTEGER, `date` INTEGER, `max_recall` INTEGER, `modifier` TEXT, `clear_type` TEXT, `comment` TEXT)"
private const val CREATE_UNIQUE_INDEX_PLAY_RESULTS_UUID_7 =
    "CREATE UNIQUE INDEX IF NOT EXISTS `index_play_results_uuid` ON play_results (`uuid`)"
private const val CREATE_VIEW_PLAY_RESULTS_CALCULATED_7 =
    "CREATE VIEW `play_results_calculated` AS SELECT\n        pr.id, pr.uuid, d.song_id, d.rating_class, pr.score, pr.pure,\n        CASE\n            WHEN ci.notes IS NOT NULL AND pr.pure IS NOT NULL AND pr.far IS NOT NULL AND ci.notes <> 0\n            THEN pr.score - FLOOR((pr.pure * 10000000.0 / ci.notes) + (pr.far * 0.5 * 100000000.0 / ci.notes))\n            ELSE NULL\n        END AS shiny_pure,\n        pr.far, pr.lost, pr.date, pr.max_recall, pr.modifier, pr.clear_type,\n        CASE\n            WHEN pr.score >= 100000000 THEN ci.constant / 10.0 + 2\n            WHEN pr.score >= 9800000 THEN ci.constant / 10.0 + 1 + (pr.score - 9800000) / 200000.0\n            ELSE MAX(ci.constant / 10.0 + (pr.score - 9500000) / 300000.0, 0)\n        END AS potential,\n        pr.comment\n    FROM difficulties d\n    JOIN charts_info ci ON d.song_id = ci.song_id AND d.rating_class = ci.rating_class\n    JOIN play_results pr ON d.song_id = pr.song_id AND d.rating_class = pr.rating_class"
private const val CREATE_VIEW_PLAY_RESULTS_BEST_7 =
    "CREATE VIEW `play_results_best` AS SELECT\n            prc.id, prc.uuid, prc.song_id, prc.rating_class, prc.score,\n            prc.pure, prc.shiny_pure, prc.far, prc.lost,\n            prc.date, prc.max_recall, prc.modifier, prc.clear_type,\n            MAX(prc.potential) AS potential,\n            prc.comment\n        FROM play_results_calculated prc\n        GROUP BY prc.song_id, prc.rating_class\n        ORDER BY prc.potential DESC"

private data class PlayResultIntermediate(
    val uuid: UUID,
    val id: Int,
    val songId: String,
    val ratingClass: String,
    val score: Int,
    val pure: Int?,
    val far: Int?,
    val lost: Int?,
    val date: Long?,
    val maxRecall: Int?,
    val modifier: String?,
    val clearType: String?,
    val comment: String?,
) {
    val uuidByteArray = uuid.let {
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(it.mostSignificantBits)
        bb.putLong(it.leastSignificantBits)
        bb.array()
    }
}

object Migration_6_7 : Migration(6, 7) {
    // TODO: we really need a migration test... why not add it now?
    override fun migrate(connection: SQLiteConnection) {
        val playResults = mutableListOf<PlayResultIntermediate>()

        connection.prepare(
            "SELECT id, song_id, rating_class, score, pure, far, lost, date, max_recall, modifier, clear_type, comment FROM play_results"
        ).use {
            while (it.step()) {
                val id = it.getInt(0)
                val songId = it.getText(1)
                val ratingClass = it.getText(2)
                val score = it.getInt(3)
                val pure = it.getIntOrNull(4)
                val far = it.getIntOrNull(5)
                val lost = it.getIntOrNull(6)
                val date = it.getLongOrNull(7)
                val maxRecall = it.getIntOrNull(8)
                val modifier = it.getTextOrNull(9)
                val clearType = it.getTextOrNull(10)
                val comment = it.getTextOrNull(11)

                playResults.add(
                    PlayResultIntermediate(
                        uuid = UUID.randomUUID(),
                        id = id,
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
                        comment = comment,
                    )
                )
            }
        }

        connection.execSQL("DROP TABLE play_results")
        connection.execSQL("DROP VIEW play_results_calculated")
        connection.execSQL("DROP VIEW play_results_best")

        connection.execSQL(CREATE_TABLE_PLAY_RESULTS_7)
        connection.execSQL(CREATE_UNIQUE_INDEX_PLAY_RESULTS_UUID_7)
        connection.execSQL(CREATE_VIEW_PLAY_RESULTS_CALCULATED_7)
        connection.execSQL(CREATE_VIEW_PLAY_RESULTS_BEST_7)

        connection.prepare(
            "INSERT INTO play_results " +
                "(`id`, `uuid`, `song_id`, `rating_class`, `score`, `pure`, `far`, `lost`, `date`, `max_recall`, `modifier`, `clear_type`, `comment`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        ).use { stmt ->
            playResults.forEach {
                stmt.bindInt(1, it.id)
                stmt.bindBlob(2, it.uuidByteArray)
                stmt.bindText(3, it.songId)
                stmt.bindText(4, it.ratingClass)
                stmt.bindInt(5, it.score)
                stmt.bindIntOrNull(6, it.pure)
                stmt.bindIntOrNull(7, it.far)
                stmt.bindIntOrNull(8, it.lost)
                stmt.bindLongOrNull(9, it.date)
                stmt.bindIntOrNull(10, it.maxRecall)
                stmt.bindTextOrNull(11, it.modifier)
                stmt.bindTextOrNull(12, it.clearType)
                stmt.bindTextOrNull(13, it.comment)

                stmt.step()
                stmt.clearBindings()
                stmt.reset()
            }
        }
    }
}
