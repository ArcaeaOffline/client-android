package xyz.sevive.arcaeaoffline.core.database.migrations

import androidx.room.RenameTable
import androidx.room.migration.AutoMigrationSpec

@RenameTable("scores", "play_results")
class AutoMigration_5_6 : AutoMigrationSpec
