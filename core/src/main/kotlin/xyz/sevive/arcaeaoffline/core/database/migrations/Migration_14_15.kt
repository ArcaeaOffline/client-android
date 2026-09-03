package xyz.sevive.arcaeaoffline.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

object Migration_14_15 : Migration(14, 15) {
    override fun migrate(connection: SQLiteConnection) {
        // Nullable raw value from upstream songlist; null means no alias.
        connection.execSQL("ALTER TABLE `difficulties` ADD COLUMN `rating_class_alias` INTEGER")

        // The `charts` view is no longer declared by the database; leaving the
        // stale definition behind would break any future `CREATE VIEW charts`.
        connection.execSQL("DROP VIEW IF EXISTS `charts`")
    }
}
