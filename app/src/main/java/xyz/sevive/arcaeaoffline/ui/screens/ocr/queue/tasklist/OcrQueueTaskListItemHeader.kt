package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.tasklist

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.helpers.context.getFilename
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.OcrQueueScreenViewModel
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import java.io.File


@Composable
private fun TaskDetailsDialog(
    uiItem: OcrQueueScreenViewModel.TaskUiItem,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
        text = {
            LazyColumn {
                item {
                    TextPreferencesWidget(
                        title = "ID",
                        content = uiItem.id.toString(),
                    )
                }

                item {
                    TextPreferencesWidget(
                        title = "Uri",
                        content = uiItem.fileUri.toString(),
                    )
                }
            }
        },
    )
}

@Composable
internal fun OcrQueueTaskListItemHeader(
    uiItem: OcrQueueScreenViewModel.TaskUiItem,
    onShowImagePreview: () -> Unit,
    onDeleteTask: () -> Unit,
    onEditChart: () -> Unit,
    onEditPlayResult: () -> Unit,
    onSaveTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val filename = remember(uiItem.fileUri) { context.getFilename(uiItem.fileUri) ?: "-" }

    var showEditPopup by rememberSaveable { mutableStateOf(false) }

    var showDetailsDialog by rememberSaveable { mutableStateOf(false) }
    if (showDetailsDialog) {
        TaskDetailsDialog(
            uiItem = uiItem,
            onDismissRequest = { showDetailsDialog = false },
        )
    }

    Column(modifier) {
        Row(
            Modifier.clickable(onClick = onShowImagePreview),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.minimumInteractiveComponentSize()) {
                OcrQueueTaskListItemStatus(uiItem.status)
            }

            Text(
                filename,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )

            Icon(
                Icons.Default.ImageSearch,
                contentDescription = null,
                Modifier.minimumInteractiveComponentSize(),
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onDeleteTask,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.general_delete),
                )
            }

            Spacer(Modifier.weight(1f))

            IconButton(onClick = { showDetailsDialog = true }) {
                Icon(Icons.Default.Info, contentDescription = null)
            }

            Box(Modifier.wrapContentSize(Alignment.TopEnd)) {
                IconButton(onClick = { showEditPopup = true }) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }

                DropdownMenu(
                    expanded = showEditPopup,
                    onDismissRequest = { showEditPopup = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.arcaea_play_result)) },
                        onClick = {
                            onEditPlayResult()
                            showEditPopup = false
                        },
                        enabled = uiItem.canEditPlayResult,
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.arcaea_chart)) },
                        onClick = {
                            onEditChart()
                            showEditPopup = false
                        },
                        enabled = uiItem.canEditChart,
                    )
                }
            }

            IconButton(
                onClick = onSaveTask,
                enabled = uiItem.status == OcrQueueTaskStatus.DONE,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OcrQueueTaskListItemHeaderPreview() {
    ArcaeaOfflineTheme {
        Surface {
            Column {
                OcrQueueTaskListItemHeader(
                    uiItem = OcrQueueScreenViewModel.TaskUiItem(
                        id = 123,
                        fileUri = Uri.fromFile(File("preview.png")),
                        status = OcrQueueTaskStatus.DONE,
                        ocrResult = null,
                        playResult = null,
                        chart = null,
                        exception = null,
                    ),
                    onShowImagePreview = {},
                    onSaveTask = {},
                    onEditChart = {},
                    onEditPlayResult = {},
                    onDeleteTask = {},
                )
            }
        }
    }
}
