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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.SubScreenTopAppBar
import xyz.sevive.arcaeaoffline.ui.components.IconRow


@Composable
fun OcrQueueAddImageFilesProgressDialog(
    addImagesFromFolderProcessing: Boolean,
    addImagesProgress: Int,
    addImagesProgressTotal: Int,
    onStopAddImageFiles: () -> Unit,
) {
    val showDialog = addImagesFromFolderProcessing || addImagesProgressTotal > -1

    val progress = if (addImagesProgress == 0) {
        0f
    } else if (addImagesProgressTotal > -1) {
        addImagesProgress.toFloat() / addImagesProgressTotal.toFloat()
    } else null
    val progressPercentage = progress?.times(100)?.toInt()

    val icon = if (progress == null) Icons.Default.HourglassBottom else Icons.Default.PhotoLibrary
    val text = if (progress == null) {
        stringResource(R.string.general_please_wait)
    } else {
        "${addImagesProgress}/${addImagesProgressTotal} ($progressPercentage%)"
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(icon, contentDescription = null) },
            text = {
                Column {
                    Text(text)
                    when (progress != null) {
                        true -> LinearProgressIndicator(progress = { progress })
                        false -> LinearProgressIndicator()
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onStopAddImageFiles,
                    enabled = addImagesProgressTotal > -1,
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
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrQueueScreen(
    onNavigateUp: () -> Unit,
    viewModel: OcrQueueViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val addImagesFromFolderProcessing by viewModel.addImagesFromFolderProcessing.collectAsState()
    val addImagesProgress by viewModel.addImagesProgress.collectAsState()
    val addImagesProgressTotal by viewModel.addImagesProgressTotal.collectAsState()

    val queueRunning by viewModel.queueRunning.collectAsState()

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        coroutineScope.launch { viewModel.addImageFiles(uris, context) }
    }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val folder = DocumentFile.fromTreeUri(context, uri)
            folder?.let { coroutineScope.launch { viewModel.addFolder(it, context) } }
        }
    }

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    if (showSettingsDialog) {
        OcrQueuePreferencesDialog(
            onDismissRequest = { showSettingsDialog = false },
            viewModel,
        )
    }

    SubScreenContainer(
        topBar = {
            SubScreenTopAppBar(onNavigateUp = onNavigateUp,
                title = { Text(stringResource(R.string.ocr_queue_title)) },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, null)
                    }
                })
        },
    ) {
        OcrQueueAddImageFilesProgressDialog(
            addImagesFromFolderProcessing = addImagesFromFolderProcessing,
            addImagesProgress = addImagesProgress,
            addImagesProgressTotal = addImagesProgressTotal,
            onStopAddImageFiles = { viewModel.stopAddImageFiles() },
        )

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
            }

            OcrQueueListWrapper(viewModel)
        }
    }
}
