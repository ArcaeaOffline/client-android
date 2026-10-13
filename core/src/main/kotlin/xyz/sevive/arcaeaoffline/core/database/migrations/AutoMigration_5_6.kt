package xyz.sevive.arcaeaoffline.core.database.migrations

import androidx.room3.RenameTable
import androidx.room3.migration.AutoMigrationSpec

@RenameTable("scores", "play_results")
class AutoMigration_5_6 : AutoMigrationSpec
