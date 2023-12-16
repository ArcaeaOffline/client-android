package xyz.sevive.arcaeaoffline.ui.ocr.queue

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.origeek.imageViewer.viewer.ImageViewer
import com.origeek.imageViewer.viewer.rememberViewerState
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard
import xyz.sevive.arcaeaoffline.ui.components.scoreeditor.ScoreEditor
import xyz.sevive.arcaeaoffline.ui.components.scoreeditor.ScoreEditorViewModel
import xyz.sevive.arcaeaoffline.ui.utils.getFilename


@Composable
fun OcrQueueItemImagePreview(task: OcrQueueTask, onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val imageViewerState = rememberViewerState()
    val filename = context.getFilename(task.fileUri) ?: "-"
    var showFileUri by rememberSaveable { mutableStateOf(false) }

    val inputStream = context.contentResolver.openInputStream(task.fileUri)
    if (inputStream == null) {
        Toast.makeText(context, "Cannot preview image", Toast.LENGTH_LONG).show()
        onDismissRequest()
        return
    }
    val image = inputStream.use { BitmapFactory.decodeStream(inputStream) }

    Dialog(
        onDismissRequest = onDismissRequest,
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
                    onTap = { onDismissRequest() }
                    onDoubleTap = { coroutineScope.launch { imageViewerState.toggleScale(it) } }
                },
            )
        }
    }
}

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
    onEditScore: (Score) -> Unit,
    onSaveScore: (OcrQueueTask) -> Unit,
    scoreEditorViewModel: ScoreEditorViewModel = viewModel(),
) {
    val context = LocalContext.current

    var showImagePreview by rememberSaveable { mutableStateOf(false) }
    var showScoreEditor by rememberSaveable { mutableStateOf(false) }
    val filename = context.getFilename(task.fileUri) ?: "-"

    if (showImagePreview) {
        OcrQueueItemImagePreview(task, onDismissRequest = { showImagePreview = false })
    }

    val score = task.score
    if (showScoreEditor && score != null) {
        scoreEditorViewModel.setArcaeaScore(score)

        Dialog(
            onDismissRequest = { showScoreEditor = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(Modifier.fillMaxWidth(0.95f)) {
                ScoreEditor(
                    onScoreCommit = { onEditScore(it); showScoreEditor = false },
                    viewModel = scoreEditorViewModel
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
                    onClick = { onSaveScore(task) },
                    enabled = score != null,
                ) {
                    Icon(Icons.Default.Save, null)
                }
                IconButton(
                    onClick = { showScoreEditor = true },
                    enabled = score != null,
                ) {
                    Icon(Icons.Default.Edit, null)
                }
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
                    val scoreCardColors = if (task.scoreValid) null else {
                        CardDefaults.cardColors(containerColor = Color.Yellow.copy(0.2f))
                    }
                    ArcaeaScoreCard(score!!, chart = task.chart, colors = scoreCardColors)
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
