package xyz.sevive.arcaeaoffline.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import xyz.sevive.arcaeaoffline.core.database.entities.Property
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository

/**
 * Stores the scoring mode while a database is being created, so what a database is
 * interpreted with is written down rather than re-derived from the fallback on every read.
 */
internal object ScoringModeCreateCallback : RoomDatabase.Callback() {
    override fun onCreate(connection: SQLiteConnection) {
        connection
            .prepare("INSERT OR IGNORE INTO `properties` (`key`, `value`) VALUES (?, ?)")
            .use { statement ->
                statement.bindText(1, Property.KEY_SCORING_MODE)
                statement.bindText(2, PropertyRepository.DEFAULT_SCORING_MODE.key.toString())
                statement.step()
            }
    }
}
