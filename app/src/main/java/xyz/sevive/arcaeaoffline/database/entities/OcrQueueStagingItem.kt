package xyz.sevive.arcaeaoffline.database.entities

import android.net.Uri
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "ocr_queue_staging_item",
    indices = [
        Index(value = ["uri"], unique = true),
        Index(value = ["batch_id"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = OcrQueueStagingBatch::class,
            parentColumns = ["id"],
            childColumns = ["batch_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class OcrQueueStagingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "batch_id")
    val batchId: Long,
    @ColumnInfo(name = "uri")
    val uri: Uri,
    @ColumnInfo(name = "uri_type")
    val uriType: OcrQueueStagingUriType,
    @ColumnInfo(name = "checked")
    val checked: Boolean = false,
    @ColumnInfo(name = "should_insert")
    val shouldInsert: Boolean = false,
)
