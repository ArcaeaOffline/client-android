package xyz.sevive.arcaeaoffline.database.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "ocr_queue_staging_batch")
data class OcrQueueStagingBatch(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "inserted_at")
    val insertedAt: Instant,
    @ColumnInfo(name = "options")
    val options: OcrQueueStagingOptions,
)
