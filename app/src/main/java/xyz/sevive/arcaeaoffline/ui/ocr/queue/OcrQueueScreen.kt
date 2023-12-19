package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.IconRow


@Composable
fun OcrQueueAddImageFilesSettingsDialog(
    onDismissRequest: () -> Unit,
    ocrQueueViewModel: OcrQueueViewModel,
) {
    val checkIsImage by ocrQueueViewModel.checkIsImage.collectAsState()
    val detectScreenshot by ocrQueueViewModel.detectScreenshot.collectAsState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        icon = { Icon(Icons.Default.Settings, null) },
        title = { Text(stringResource(R.string.ocr_queue_add_image_options_title)) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checkIsImage, { ocrQueueViewModel.setCheckIsImage(it) })
                    Text(stringResource(R.string.ocr_queue_add_image_options_check_is_image))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(detectScreenshot, { ocrQueueViewModel.setDetectScreenshot(it) })
                    Text(stringResource(R.string.ocr_queue_add_image_options_detect_screenshot))
                }
            }
        },
    )
}

@Composable
fun OcrQueueAddImageFilesProgressDialog(ocrQueueViewModel: OcrQueueViewModel) {
    val addImagesProcessing by ocrQueueViewModel.addImagesProcessing.collectAsState()
    val addImagesProgress by ocrQueueViewModel.addImagesProgress.collectAsState()
    val addImagesProgressTotal by ocrQueueViewModel.addImagesProgressTotal.collectAsState()

    if (addImagesProcessing && addImagesProgressTotal < 0) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            icon = { Icon(Icons.Default.HourglassBottom, null) },
            title = { Text(stringResource(R.string.general_please_wait)) },
            text = { LinearProgressIndicator() },
        )
    }

    if (addImagesProgressTotal > -1) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(
                    onClick = { ocrQueueViewModel.stopAddImageFiles() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    IconRow(icon = { Icon(Icons.Default.Stop, null) }) {
                        Text(stringResource(R.string.general_stop))
                    }
                }
            },
            icon = { Icon(Icons.Default.PhotoLibrary, null) },
            text = {
                Column {
                    Text("$addImagesProgress/$addImagesProgressTotal")
                    LinearProgressIndicator(progress = { addImagesProgress.toFloat() / addImagesProgressTotal })
                }
            },
        )
    }
}

@Composable
fun OcrQueueScreen(
    onNavigateUp: () -> Unit,
    ocrQueueViewModel: OcrQueueViewModel = viewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val queueRunning by ocrQueueViewModel.queueRunning.collectAsState()

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        coroutineScope.launch { ocrQueueViewModel.addImageFiles(uris, context) }
    }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val folder = DocumentFile.fromTreeUri(context, uri)
            folder?.let { coroutineScope.launch { ocrQueueViewModel.addFolder(it, context) } }
        }
    }

    OcrQueueAddImageFilesProgressDialog(ocrQueueViewModel)

    var showAddImageFilesSettingsDialog by rememberSaveable { mutableStateOf(false) }
    if (showAddImageFilesSettingsDialog) {
        OcrQueueAddImageFilesSettingsDialog(
            onDismissRequest = { showAddImageFilesSettingsDialog = false },
            ocrQueueViewModel,
        )
    }

    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        title = stringResource(R.string.ocr_queue_title),
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))) {
                Button(
                    onClick = { pickImagesLauncher.launch("image/*") },
                    Modifier.weight(1f),
                    enabled = !queueRunning,
                ) {
                    IconRow(icon = { Icon(Icons.Default.PhotoLibrary, null) }) {
                        Text(stringResource(R.string.ocr_queue_add_images_button))
                    }
                }

                Button(
                    onClick = { folderLauncher.launch(null) },
                    Modifier.weight(1f),
                    enabled = !queueRunning,
                ) {
                    IconRow(icon = { Icon(Icons.Default.PermMedia, null) }) {
                        Text(stringResource(R.string.ocr_queue_import_folder_button))
                    }
                }

                IconButton(onClick = { showAddImageFilesSettingsDialog = true }) {
                    Icon(Icons.Default.Settings, null)
                }
            }

            OcrQueue(ocrQueueViewModel)
        }
    }
}
