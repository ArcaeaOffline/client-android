package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.staging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.Progress
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.LinearProgressIndicatorWrapper
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OcrQueueStagingSheet(
    onDismissRequest: () -> Unit,
    onPickImages: () -> Unit,
    onPickFolder: () -> Unit,
    onStartJob: () -> Unit,
    onStopJob: () -> Unit,
    onDeleteAll: () -> Unit,
    isJobRunning: Boolean,
    workerProgress: Progress?,
    stagingItemCount: OcrQueueStagingViewModel.StagingItemCount?,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
        BottomSheetContent(
            onPickImages = onPickImages,
            onPickFolder = onPickFolder,
            onStartJob = onStartJob,
            onStopJob = onStopJob,
            onDeleteAll = onDeleteAll,
            isJobRunning = isJobRunning,
            workerProgress = workerProgress,
            stagingItemCount = stagingItemCount,
        )
    }
}

@Composable
private fun PickerButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        Modifier
            .clickable(onClick = onClick)
            .height(IntrinsicSize.Min)
            .padding(vertical = 36.dp)
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                icon,
                contentDescription = null,
                Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun StagingItemStatus(
    stagingItemCount: OcrQueueStagingViewModel.StagingItemCount?,
    modifier: Modifier = Modifier,
) {
    if (stagingItemCount != null) {
        LinearProgressIndicatorWrapper(
            current = stagingItemCount.checked,
            total = stagingItemCount.total,
            modifier = modifier,
        )
    } else {
        Text(
            stringResource(R.string.ocr_queue_staging_empty),
            modifier = modifier,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun StagingItemAction(
    onDeleteAll: () -> Unit,
    stagingItemCount: OcrQueueStagingViewModel.StagingItemCount?,
    isJobRunning: Boolean,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onDeleteAll,
        enabled = stagingItemCount != null && !isJobRunning,
        colors =
            ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        modifier = modifier,
    ) {
        IconRow {
            Icon(Icons.Default.Close, contentDescription = null)
            Text(stringResource(R.string.ocr_queue_clear_staging_button))
        }
    }
}

@Composable
private fun WorkerStatus(
    workerProgress: Progress?,
    modifier: Modifier = Modifier,
) {
    if (workerProgress != null) {
        LinearProgressIndicatorWrapper(
            workerProgress,
            modifier = modifier,
        )
    } else {
        Text(
            stringResource(R.string.ocr_queue_validation_not_running),
            modifier = modifier,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun WorkerAction(
    onStartJob: () -> Unit,
    onStopJob: () -> Unit,
    isJobRunning: Boolean,
    stagingItemCount: OcrQueueStagingViewModel.StagingItemCount?,
    modifier: Modifier = Modifier,
) {
    var isJobControlButtonInCooldown by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isJobControlButtonInCooldown) {
        if (isJobControlButtonInCooldown) {
            delay(1.seconds)
            isJobControlButtonInCooldown = false
        }
    }

    val icon = if (isJobRunning) Icons.Default.Stop else Icons.Default.PlayArrow
    val label =
        if (isJobRunning) {
            stringResource(R.string.general_stop)
        } else {
            stringResource(R.string.ocr_queue_start_staging_job_button)
        }
    val buttonColors =
        if (isJobRunning) {
            ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        } else {
            ButtonDefaults.filledTonalButtonColors()
        }

    FilledTonalButton(
        onClick = {
            if (isJobRunning) onStopJob() else onStartJob()
            isJobControlButtonInCooldown = true
        },
        enabled = !isJobControlButtonInCooldown && stagingItemCount != null,
        colors = buttonColors,
        modifier = modifier,
    ) {
        IconRow {
            Icon(icon, contentDescription = null)
            Text(label)
        }
    }
}

@OptIn(ExperimentalGridApi::class)
@Composable
private fun BottomSheetContent(
    onPickImages: () -> Unit,
    onPickFolder: () -> Unit,
    onStartJob: () -> Unit,
    onStopJob: () -> Unit,
    onDeleteAll: () -> Unit,
    isJobRunning: Boolean,
    workerProgress: Progress?,
    stagingItemCount: OcrQueueStagingViewModel.StagingItemCount?,
) {
    Column {
        Grid(
            config = {
                column(GridTrackSize.MaxContent)
                // Prevents the inner progress indicator's intrinsic width from expanding this column
                column(GridTrackSize.MinMax(0.dp, 1.fr))
                column(GridTrackSize.MaxContent)
                columnGap(24.dp)
                rowGap(16.dp)
            },
            Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
        ) {
            Text(
                stringResource(R.string.ocr_queue_staging_status),
                Modifier.gridItem(alignment = Alignment.CenterEnd),
            )

            StagingItemStatus(
                stagingItemCount = stagingItemCount,
                Modifier
                    .fillMaxWidth()
                    .gridItem(alignment = Alignment.Center),
            )

            StagingItemAction(
                onDeleteAll = onDeleteAll,
                stagingItemCount = stagingItemCount,
                isJobRunning = isJobRunning,
                Modifier.fillMaxWidth().gridItem(alignment = Alignment.CenterStart),
            )

            Text(
                stringResource(R.string.ocr_queue_validation_status),
                Modifier.fillMaxWidth().gridItem(alignment = Alignment.CenterEnd),
            )

            WorkerStatus(
                workerProgress = workerProgress,
                Modifier
                    .fillMaxWidth()
                    .gridItem(alignment = Alignment.Center),
            )

            WorkerAction(
                onStartJob = onStartJob,
                onStopJob = onStopJob,
                isJobRunning = isJobRunning,
                stagingItemCount = stagingItemCount,
                Modifier.gridItem(alignment = Alignment.CenterStart),
            )
        }

        Row {
            PickerButton(
                onClick = onPickImages,
                icon = Icons.Default.PhotoLibrary,
                label = stringResource(R.string.ocr_queue_pick_images_button),
                modifier = Modifier.weight(1f),
            )

            PickerButton(
                onClick = onPickFolder,
                icon = ImageVector.vectorResource(R.drawable.ic_folder_image),
                label = stringResource(R.string.ocr_queue_pick_folder_button),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun BottomSheetContentPreview() {
    ArcaeaOfflineTheme {
        Surface {
            BottomSheetContent(
                onPickImages = {},
                onPickFolder = {},
                onStartJob = {},
                onStopJob = {},
                onDeleteAll = {},
                isJobRunning = false,
                workerProgress = Progress(3, 10),
                stagingItemCount =
                    OcrQueueStagingViewModel.StagingItemCount(
                        checked = 3,
                        total = 10,
                    ),
            )
        }
    }
}
