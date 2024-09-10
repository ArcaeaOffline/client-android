package xyz.sevive.arcaeaoffline.database.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "ocr_queue_enqueue_buffer",
    indices = [Index(value = ["uri"], unique = true)],
)
data class OcrQueueEnqueueBuffer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: Uri,
    val checked: Boolean = false,
    @ColumnInfo(name = "should_insert") val shouldInsert: Boolean = false,
)
