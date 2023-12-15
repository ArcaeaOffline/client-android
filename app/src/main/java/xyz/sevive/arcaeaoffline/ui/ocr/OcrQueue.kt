package xyz.sevive.arcaeaoffline.ui.ocr

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.origeek.imageViewer.viewer.ImageViewer
import com.origeek.imageViewer.viewer.rememberViewerState
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard
import xyz.sevive.arcaeaoffline.ui.utils.getFilename


@Composable
fun OcrQueueItemStatus(status: OcrQueueStatus) {
    when (status) {
        OcrQueueStatus.IDLE -> Icon(Icons.Default.HistoryToggleOff, null)

        OcrQueueStatus.PROCESSING -> CircularProgressIndicator(
            Modifier.size(Icons.Default.HourglassBottom.defaultHeight),
            strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth / 1.5f,
        )

        OcrQueueStatus.DONE -> Icon(
            Icons.Default.Check,
            null,
            tint = MaterialTheme.colorScheme.secondary,
        )

        OcrQueueStatus.ERROR -> Icon(
            Icons.Default.Error,
            null,
            tint = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
fun OcrQueueItem(
    task: OcrQueueTask,
    onDeleteTask: (OcrQueueTask) -> Unit,
    deleteEnabled: Boolean = true,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val imageViewerState = rememberViewerState()
    var showImagePreview by rememberSaveable { mutableStateOf(false) }
    val filename = context.getFilename(task.fileUri) ?: "-"
    var showFileUri by rememberSaveable { mutableStateOf(false) }

    if (showImagePreview) {
        val inputStream = context.contentResolver.openInputStream(task.fileUri)
        if (inputStream == null) {
            Toast.makeText(context, "Cannot preview image", Toast.LENGTH_LONG).show()
            showImagePreview = false
            return
        }
        val image = inputStream.use { BitmapFactory.decodeStream(inputStream) }

        Dialog(
            onDismissRequest = { showImagePreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Column(Modifier.fillMaxSize()) {
                Surface(Modifier.fillMaxWidth()) {
                    if (showFileUri) {
                        Text(
                            task.fileUri.toString(),
                            Modifier.clickable { showFileUri = !showFileUri },
                        )
                    } else {
                        Text(
                            filename,
                            Modifier.clickable { showFileUri = !showFileUri },
                        )
                    }
                }

                // TODO: minSdk 24
                ImageViewer(
                    state = imageViewerState,
                    model = image.asImageBitmap(),
                    modifier = Modifier.fillMaxSize(),
                    detectGesture = {
                        onTap = { showImagePreview = false }
                        onDoubleTap = { coroutineScope.launch { imageViewerState.toggleScale(it) } }
                    },
                )
            }
        }
    }

    Card {
        Column(
            Modifier.padding(dimensionResource(R.dimen.general_card_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.general_icon_text_padding)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "ID ${task.id}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        filename,
                        Modifier.clickable { showImagePreview = true },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Spacer(Modifier.weight(0.1f))

                OcrQueueItemStatus(task.status)
                IconButton(
                    onClick = { onDeleteTask(task) },
                    enabled = deleteEnabled,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                ) {
                    Icon(Icons.Default.Delete, null)
                }
            }

            when (task.status) {
                OcrQueueStatus.DONE -> Row {
                    val score = task.score!!
                    ArcaeaScoreCard(score)
                }

                OcrQueueStatus.ERROR -> Text(
                    task.exception!!.message ?: task.status.toString(),
                    color = MaterialTheme.colorScheme.error,
                )

                else -> {}
            }
        }
    }
}

@Composable
fun OcrQueue(ocrQueueViewModel: OcrQueueViewModel) {
    val tasks by ocrQueueViewModel.ocrQueueTasks.collectAsState()
    val queueRunning by ocrQueueViewModel.queueRunning.collectAsState()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))) {
        items(tasks) { task ->
            OcrQueueItem(
                task,
                onDeleteTask = { ocrQueueViewModel.deleteTask(it.id) },
                deleteEnabled = !queueRunning,
            )
        }
    }
}
