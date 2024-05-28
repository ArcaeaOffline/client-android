package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Dialog
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.helpers.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.helpers.context.getFilename
import xyz.sevive.arcaeaoffline.ui.common.imagepreview.ImagePreviewDialog
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard
import xyz.sevive.arcaeaoffline.ui.components.ScoreValidatorWarningDetails
import xyz.sevive.arcaeaoffline.ui.components.scoreeditor.ScoreEditorDialog

@Composable
fun OcrQueueItemImagePreview(uiItem: OcrQueueTaskUiItem, onDismissRequest: () -> Unit) {
    val context = LocalContext.current

    val filename = context.getFilename(uiItem.fileUri) ?: "-"
    var showFileUri by rememberSaveable { mutableStateOf(false) }
    val displayText = if (showFileUri) uiItem.fileUri.toString() else filename

    ImagePreviewDialog(
        inputStream = context.contentResolver.openInputStream(uiItem.fileUri),
        onDismiss = onDismissRequest,
        topBarContent = {
            Text(
                displayText,
                Modifier
                    .fillMaxWidth()
                    .clickable { showFileUri = !showFileUri },
            )
        },
    )
}

@Composable
fun OcrQueueItemStatus(status: OcrQueueTaskStatus) {
    when (status) {
        OcrQueueTaskStatus.IDLE -> Icon(
            Icons.Default.HistoryToggleOff,
            contentDescription = stringResource(R.string.ocr_queue_status_idle),
        )

        OcrQueueTaskStatus.PROCESSING -> CircularProgressIndicator(
            Modifier.size(Icons.Default.HourglassBottom.defaultHeight),
            strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth / 1.5f,
        )

        OcrQueueTaskStatus.DONE -> Icon(
            Icons.Default.Check,
            contentDescription = stringResource(R.string.ocr_queue_status_done),
            tint = MaterialTheme.colorScheme.secondary,
        )

        OcrQueueTaskStatus.ERROR -> Icon(
            Icons.Default.Error,
            contentDescription = stringResource(R.string.ocr_queue_status_error),
            tint = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
fun OcrQueueItemHeader(
    uiItem: OcrQueueTaskUiItem,
    onDeleteTask: () -> Unit,
    deleteEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val filename = context.getFilename(uiItem.fileUri) ?: "-"

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                "ID ${uiItem.id}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                filename,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        Spacer(Modifier.weight(0.1f))

        OcrQueueItemStatus(uiItem.status)

        IconButton(
            onClick = onDeleteTask,
            enabled = deleteEnabled,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(R.string.general_delete),
            )
        }
    }
}


@Composable
fun OcrQueueItemScore(
    uiItem: OcrQueueTaskUiItem,
    onShowEditDialog: () -> Unit,
    onSaveScore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val score = uiItem.score ?: return
    val warnings = uiItem.scoreValidatorWarnings()

    var showWarningsDialog by rememberSaveable { mutableStateOf(false) }
    if (showWarningsDialog && warnings.isNotEmpty()) {
        Dialog(onDismissRequest = { showWarningsDialog = false }) {
            Card {
                ScoreValidatorWarningDetails(warnings)
            }
        }
    }

    Row(
        modifier,
        verticalAlignment = Alignment.Bottom,
    ) {
        ArcaeaScoreCard(score, chart = uiItem.chart, modifier = Modifier.weight(1f))

        Column {
            if (warnings.isNotEmpty()) {
                IconButton(
                    onClick = { showWarningsDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    )
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                }
            }

            IconButton(onClick = onShowEditDialog) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.general_edit))
            }

            IconButton(
                onClick = onSaveScore, colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = stringResource(R.string.general_save))
            }
        }
    }
}

@Composable
fun OcrQueueItem(
    uiItem: OcrQueueTaskUiItem,
    onDeleteTask: (Int) -> Unit,
    deleteEnabled: Boolean = true,
    onEditScore: (Int, Score) -> Unit,
    onSaveScore: (Int) -> Unit,
) {
    var showImagePreview by rememberSaveable { mutableStateOf(false) }
    var showScoreEditor by rememberSaveable { mutableStateOf(false) }

    if (showImagePreview) {
        OcrQueueItemImagePreview(uiItem, onDismissRequest = { showImagePreview = false })
    }

    val score = uiItem.score
    if (showScoreEditor && score != null) {
        ScoreEditorDialog(
            onDismiss = { showScoreEditor = false },
            score = score,
            onScoreChange = { onEditScore(uiItem.id, it) },
        )
    }

    OutlinedCard {
        Card(onClick = { showImagePreview = true }) {
            OcrQueueItemHeader(
                uiItem = uiItem,
                onDeleteTask = { onDeleteTask(uiItem.id) },
                deleteEnabled = deleteEnabled,
                modifier = Modifier.padding(dimensionResource(R.dimen.card_padding)),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_text_padding))) {
            val contentModifier = Modifier.padding(dimensionResource(R.dimen.card_padding))

            when (uiItem.status) {
                OcrQueueTaskStatus.DONE -> OcrQueueItemScore(
                    uiItem = uiItem,
                    onShowEditDialog = { showScoreEditor = true },
                    onSaveScore = { onSaveScore(uiItem.id) },
                    modifier = contentModifier,
                )

                OcrQueueTaskStatus.ERROR -> {
                    val exception = uiItem.exception!!
                    Text(
                        buildAnnotatedString {
                            withStyle(MaterialTheme.typography.labelMedium.toParagraphStyle()) {
                                appendLine(exception::class.qualifiedName)
                            }
                            append(exception.message)
                        },
                        color = MaterialTheme.colorScheme.error,
                        modifier = contentModifier,
                    )
                }

                else -> {}
            }
        }
    }
}
