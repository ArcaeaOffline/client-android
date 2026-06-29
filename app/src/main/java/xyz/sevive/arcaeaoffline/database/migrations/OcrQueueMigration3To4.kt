package xyz.sevive.arcaeaoffline.database.migrations

import androidx.room.RenameTable
import androidx.room.migration.AutoMigrationSpec

@RenameTable(fromTableName = "ocr_queue_enqueue_buffer", toTableName = "ocr_queue_staging_item")
@RenameTable(fromTableName = "ocr_queue_enqueue_batches", toTableName = "ocr_queue_staging_batch")
class OcrQueueMigration3To4 : AutoMigrationSpec
