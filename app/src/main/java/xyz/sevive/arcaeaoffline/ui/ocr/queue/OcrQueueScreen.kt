package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.SubScreenTopAppBar
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.LinearProgressIndicatorWrapper


@Composable
internal fun OcrQueueAddImageFilesProgressDialog(
    addImagesFromFolderProcessing: Boolean,
    addImagesProgress: Int,
    addImagesProgressTotal: Int,
    onStopAddImageFiles: () -> Unit,
) {
    val showDialog = addImagesFromFolderProcessing || addImagesProgressTotal > -1

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null) },
            text = {
                LinearProgressIndicatorWrapper(
                    current = addImagesProgress,
                    total = addImagesProgressTotal,
                    indeterminateLabel = stringResource(R.string.general_please_wait)
                )
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
    viewModel: OcrQueueScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current

    val addImagesFromFolderProcessing by viewModel.addImagesFromFolderProcessing.collectAsStateWithLifecycle()
    val addImagesProgress by viewModel.addImagesProgress.collectAsStateWithLifecycle()
    val addImagesProgressTotal by viewModel.addImagesProgressTotal.collectAsStateWithLifecycle()

    val uiItems by viewModel.uiItems.collectAsStateWithLifecycle()
    val idleUiItems by viewModel.idleUiItems.collectAsStateWithLifecycle()
    val processingUiItems by viewModel.processingUiItems.collectAsStateWithLifecycle()
    val doneUiItems by viewModel.doneUiItems.collectAsStateWithLifecycle()
    val doneWithWarningUiItems by viewModel.doneWithWarningUiItems.collectAsStateWithLifecycle()
    val errorUiItems by viewModel.errorUiItems.collectAsStateWithLifecycle()

    val queueRunning by viewModel.queueRunning.collectAsStateWithLifecycle()

    var category by rememberOcrQueueScreenCategory()

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> viewModel.addImageFiles(uris, context) }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            val folder = DocumentFile.fromTreeUri(context, uri)
            folder?.let { viewModel.addFolder(it, context) }
        }
    }

    var showPreferencesDialog by rememberSaveable { mutableStateOf(false) }
    val preferencesUiState by viewModel.preferencesUiState.collectAsStateWithLifecycle()
    if (showPreferencesDialog) {
        OcrQueuePreferencesDialog(
            onDismissRequest = { showPreferencesDialog = false },
            uiState = preferencesUiState,
            onSetCheckIsImage = { viewModel.setCheckIsImage(it) },
            onSetCheckIsArcaeaImage = { viewModel.setCheckIsArcaeaImage(it) },
            onSetParallelCount = { viewModel.setParallelCount(it) },
        )
    }

    SubScreenContainer(
        topBar = {
            SubScreenTopAppBar(onNavigateUp = onNavigateUp,
                title = { Text(stringResource(R.string.ocr_queue_title)) },
                actions = {
                    IconButton(onClick = { showPreferencesDialog = true }) {
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
            OcrQueueAddTaskActions(
                onPickImages = { pickImagesLauncher.launch("image/*") },
                onPickFolder = { folderLauncher.launch(null) },
                enabled = !queueRunning,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(visible = category != OcrQueueScreenCategory.NULL) {
                    IconButton(onClick = { category = OcrQueueScreenCategory.NULL }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                }

                OcrQueueProgressIndicator(
                    total = uiItems.size,
                    processing = processingUiItems.size,
                    done = doneUiItems.size,
                    error = errorUiItems.size,
                    modifier = Modifier.weight(1f),
                )

                OcrQueueActions(
                    onStartQueue = { viewModel.startQueue(context) },
                    onStopQueue = { viewModel.tryStopQueue() },
                    onClearAllTasks = { viewModel.clearTasks() },
                    queueRunning = queueRunning,
                )
            }

            OcrQueueScreenCategorySubScreen(
                category = category,
                onCategoryChange = { category = it },
                idleUiItems = idleUiItems,
                processingUiItems = processingUiItems,
                doneUiItems = doneUiItems,
                doneWithWarningUiItems = doneWithWarningUiItems,
                errorUiItems = errorUiItems,
                onSavePlayResult = { viewModel.saveTaskScore(it) },
                onDeleteTask = { viewModel.deleteTask(it) },
                onEditPlayResult = { id, pr -> viewModel.modifyTaskScore(id, pr) },
                onSaveAllTasks = { viewModel.saveAllTaskPlayResults() }
            )
        }
    }
}
