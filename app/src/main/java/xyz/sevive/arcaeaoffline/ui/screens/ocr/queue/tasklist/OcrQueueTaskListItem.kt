package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.tasklist

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.source
import kotlinx.io.asInputStream
import kotlinx.io.buffered
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.helpers.context.getFilename
import xyz.sevive.arcaeaoffline.ui.common.imagepreview.ImagePreviewDialog
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaChartSelector
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultEditorDialog
import xyz.sevive.arcaeaoffline.ui.components.BasicAlertDialogSurface
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.OcrQueueScreenViewModel

@Composable
private fun OcrQueueTaskListItemImagePreviewDialog(
    fileUri: Uri,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current

    val filename = context.getFilename(fileUri) ?: "-"
    var showFileUri by rememberSaveable { mutableStateOf(false) }
    val displayText = if (showFileUri) fileUri.toString() else filename

    ImagePreviewDialog(
        inputStream = PlatformFile(fileUri).source().buffered().asInputStream(),
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
internal fun OcrQueueTaskListItem(
    uiItem: OcrQueueScreenViewModel.TaskUiItem,
    onDelete: () -> Unit,
    onEditChart: (Chart) -> Unit,
    onEditPlayResult: (PlayResult) -> Unit,
    onSaveTask: () -> Unit,
) {
    var showImagePreview by rememberSaveable { mutableStateOf(false) }
    if (showImagePreview) {
        OcrQueueTaskListItemImagePreviewDialog(
            uiItem.dbItem.fileUri,
            onDismissRequest = { showImagePreview = false },
        )
    }

    val chart = uiItem.chart
    var showChartEditor by rememberSaveable { mutableStateOf(false) }
    if (showChartEditor) {
        BasicAlertDialogSurface(onDismissRequest = { showChartEditor = false }) {
            ArcaeaChartSelector(
                chart = chart,
                onChartChange = { it?.let(onEditChart) },
            )
        }
    }

    val playResult = uiItem.dbItem.playResult
    var showPlayResultEditor by rememberSaveable { mutableStateOf(false) }
    if (showPlayResultEditor && playResult != null) {
        ArcaeaPlayResultEditorDialog(
            onDismiss = { showPlayResultEditor = false },
            playResult = playResult,
            onPlayResultChange = { onEditPlayResult(it) },
        )
    }

    OutlinedCard {
        OcrQueueTaskListItemHeader(
            uiItem = uiItem,
            onShowImagePreview = { showImagePreview = true },
            onDeleteTask = { onDelete() },
            onEditChart = { showChartEditor = true },
            onEditPlayResult = { showPlayResultEditor = true },
            onSaveTask = onSaveTask,
        )

        OcrQueueTaskListItemResult(
            uiItem,
            Modifier.padding(dimensionResource(R.dimen.card_padding)),
        )
    }
}
