package xyz.sevive.arcaeaoffline.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Writes the scoring mode for databases that predate the `scoring_mode` property, so a
 * later change of the fallback default cannot reinterpret their play results.
 *
 * The property key and the mode value are spelled out rather than taken from
 * `Property.KEY_SCORING_MODE` and `ArcaeaScoringMode.B50`: what a migration writes has to
 * keep its meaning after those declarations change. An existing row is preserved, since
 * the property may already hold a mode the user chose.
 */
object Migration_15_16 : Migration(15, 16) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "INSERT OR IGNORE INTO `properties` (`key`, `value`) VALUES ('scoring_mode', '20260827')",
        )
    }
}
