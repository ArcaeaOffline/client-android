package xyz.sevive.arcaeaoffline.core.database.migrations

import androidx.room3.DeleteColumn
import androidx.room3.RenameColumn
import androidx.room3.migration.AutoMigrationSpec

@DeleteColumn("songs_localized", "search_title")
@DeleteColumn("songs_localized", "search_artist")
@RenameColumn("songs_localized", "source_title", "source")
class AutoMigration_9_10 : AutoMigrationSpec
