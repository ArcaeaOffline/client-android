package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.toAndroidUri
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.context.persistUriPermissions
import xyz.sevive.arcaeaoffline.jobs.OcrQueueJob
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.SubScreenTopAppBar
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.enqueuechecker.OcrQueueEnqueueCheckerBottomSheet
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.enqueuechecker.OcrQueueEnqueueCheckerFloatingActionButton
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.enqueuechecker.OcrQueueEnqueueCheckerViewModel
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.preferences.OcrQueuePreferencesBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrQueueScreen(
    viewModel: OcrQueueScreenViewModel = koinViewModel(),
    enqueueCheckerViewModel: OcrQueueEnqueueCheckerViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val queueStatus by viewModel.queueStatusUiState.collectAsStateWithLifecycle()
    val queueTaskCounts by viewModel.queueTaskCounts.collectAsStateWithLifecycle()

    val taskUiItems by viewModel.currentScreenUiItems.collectAsStateWithLifecycle()
    val isTaskUiItemsLoading by viewModel.isTaskUiItemsLoading.collectAsStateWithLifecycle()

    val category by viewModel.currentScreenCategory.collectAsStateWithLifecycle()
    val categoryBackButtonEnabled by remember {
        derivedStateOf { category != OcrQueueScreenCategory.NULL }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val enqueueCheckerUiState by enqueueCheckerViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(enqueueCheckerViewModel, lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            enqueueCheckerViewModel.events.collect { event ->
                val message = event.asString(resources)
                snackbarHostState.showSnackbar(message, withDismissAction = true)
            }
        }
    }

    val pickImagesLauncher =
        rememberFilePickerLauncher(
            mode = FileKitMode.Multiple(),
            type = FileKitType.Image,
        ) { files: List<PlatformFile>? ->
            val uris = files?.map { it.toAndroidUri() }.orEmpty()
            context.persistUriPermissions(uris, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            enqueueCheckerViewModel.addImageFiles(uris)
        }

    val pickFolderLauncher =
        rememberDirectoryPickerLauncher { dir ->
            dir?.let {
                context.persistUriPermissions(it.toAndroidUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION)
                enqueueCheckerViewModel.addFolder(it)
            }
        }

    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val fabVisible = !queueStatus.isRunning

    LaunchedEffect(fabVisible) {
        if (!fabVisible) showBottomSheet = false
    }

    if (showBottomSheet) {
        OcrQueueEnqueueCheckerBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            onPickImagesRequest = { pickImagesLauncher.launch() },
            onPickFolderRequest = { pickFolderLauncher.launch() },
            onStartJobRequest = { enqueueCheckerViewModel.requestWork() },
            onStopJobRequest = { enqueueCheckerViewModel.cancelWork() },
            onClearBufferRequest = { enqueueCheckerViewModel.clearBuffer() },
            isJobRunning = enqueueCheckerUiState.isEnqueueCheckerRunning,
            workerProgress = enqueueCheckerUiState.workerProgress,
            bufferItemsCount = enqueueCheckerUiState.bufferItemsCount,
        )
    }

    var showPreferencesDialog by rememberSaveable { mutableStateOf(false) }
    if (showPreferencesDialog) {
        OcrQueuePreferencesBottomSheet(onDismissRequest = { showPreferencesDialog = false })
    }

    SubScreenContainer(
        topBar = {
            SubScreenTopAppBar(
                title = { Text(stringResource(R.string.ocr_queue_title)) },
                actions = {
                    IconButton(onClick = { showPreferencesDialog = true }) {
                        Icon(Icons.Default.Settings, null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            OcrQueueEnqueueCheckerFloatingActionButton(
                onClick = { showBottomSheet = true },
                isVisible = fabVisible,
                badgeCount = enqueueCheckerUiState.bufferItemsCount?.total,
            )
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.setCurrentScreenCategory(OcrQueueScreenCategory.NULL) },
                    enabled = categoryBackButtonEnabled,
                ) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                }

                OcrQueueProgressIndicator(
                    taskCounts = queueTaskCounts,
                    modifier = Modifier.weight(1f),
                )

                OcrQueueActions(
                    onStartQueue = { viewModel.startQueue() },
                    onStopQueue = { viewModel.tryStopQueue() },
                    onClearAllTasks = { viewModel.clearTasks() },
                    queueRunning = queueStatus.isRunning,
                )
            }

            OcrQueueScreenCategorySubScreen(
                category = category,
                onCategoryChange = { viewModel.setCurrentScreenCategory(it) },
                taskCounts = queueTaskCounts,
                taskUiItems = taskUiItems,
                taskUiItemsLoading = isTaskUiItemsLoading,
                onSaveTask = { viewModel.saveTaskPlayResult(it) },
                onDeleteTask = { viewModel.deleteTask(it) },
                onEditChart = { id, c -> viewModel.modifyTaskChart(id, c) },
                onEditPlayResult = { id, pr -> viewModel.modifyTaskPlayResult(id, pr) },
                onSaveAllTasks = { viewModel.saveAllTaskPlayResults() },
                onStartSmartFix = { viewModel.startQueue(OcrQueueJob.RunMode.SMART_FIX) },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
