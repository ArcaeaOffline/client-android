package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import xyz.sevive.arcaeaoffline.helpers.OcrQueueJob
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.SubScreenTopAppBar
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.preferences.OcrQueuePreferencesBottomSheet


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrQueueScreen(
    onNavigateUp: () -> Unit,
    viewModel: OcrQueueScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current

    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val idleCount by viewModel.idleCount.collectAsStateWithLifecycle()
    val processingCount by viewModel.processingCount.collectAsStateWithLifecycle()
    val doneCount by viewModel.doneCount.collectAsStateWithLifecycle()
    val doneWithWarningCount by viewModel.doneWithWarningCount.collectAsStateWithLifecycle()
    val errorCount by viewModel.errorCount.collectAsStateWithLifecycle()

    val currentUiItems by viewModel.currentUiItems.collectAsStateWithLifecycle()
    val currentUiItemsLoading by viewModel.currentUiItemsLoading.collectAsStateWithLifecycle()

    val queueRunning by viewModel.queueRunning.collectAsStateWithLifecycle()

    val enqueueCheckerJobUiState by viewModel.enqueueCheckerJobUiState.collectAsStateWithLifecycle()

    val category by viewModel.currentScreenCategory.collectAsStateWithLifecycle()

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val permissionFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        uris.forEach { context.contentResolver.takePersistableUriPermission(it, permissionFlags) }
        viewModel.addImageFiles(uris)
    }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // persistent access permission to this folder
            val permissionFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, permissionFlags)

            val folder = DocumentFile.fromTreeUri(context, uri)
            folder?.let { viewModel.addFolder(it) }
        }
    }

    var showPreferencesDialog by rememberSaveable { mutableStateOf(false) }
    if (showPreferencesDialog) {
        OcrQueuePreferencesBottomSheet(
            onDismissRequest = { showPreferencesDialog = false }
        )
    }

    SubScreenContainer(
        topBar = {
            SubScreenTopAppBar(
                onNavigateUp = onNavigateUp,
                title = { Text(stringResource(R.string.ocr_queue_title)) },
                actions = {
                    IconButton(onClick = { showPreferencesDialog = true }) {
                        Icon(Icons.Default.Settings, null)
                    }
                },
            )
        },
        floatingActionButton = {
            OcrQueueAddTaskFloatingActionButton(
                onPickImages = { pickImagesLauncher.launch("image/*") },
                onPickFolder = { folderLauncher.launch(null) },
                onStopJob = { viewModel.stopAddImageFiles() },
                enabled = !queueRunning,
                uiState = enqueueCheckerJobUiState,
            )
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(visible = category != OcrQueueScreenCategory.NULL) {
                    IconButton(
                        onClick = {
                            viewModel.setCurrentScreenCategory(OcrQueueScreenCategory.NULL)
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                    }
                }

                OcrQueueProgressIndicator(
                    total = totalCount,
                    processing = processingCount,
                    done = doneCount,
                    error = errorCount,
                    modifier = Modifier.weight(1f),
                )

                OcrQueueActions(
                    onStartQueue = { viewModel.startQueue() },
                    onStopQueue = { viewModel.tryStopQueue() },
                    onClearAllTasks = { viewModel.clearTasks() },
                    queueRunning = queueRunning,
                )
            }

            OcrQueueScreenCategorySubScreen(
                category = category,
                onCategoryChange = { viewModel.setCurrentScreenCategory(it) },
                idleCount = idleCount,
                processingCount = processingCount,
                doneCount = doneCount,
                doneWithWarningCount = doneWithWarningCount,
                errorCount = errorCount,
                currentUiItems = currentUiItems,
                currentUiItemsLoading = currentUiItemsLoading,
                onSavePlayResult = { viewModel.saveTaskScore(it) },
                onDeleteTask = { viewModel.deleteTask(it) },
                onEditPlayResult = { id, pr -> viewModel.modifyTaskScore(id, pr) },
                onSaveAllTasks = { viewModel.saveAllTaskPlayResults() },
                onStartSmartFix = { viewModel.startQueue(OcrQueueJob.RunMode.SMART_FIX) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
