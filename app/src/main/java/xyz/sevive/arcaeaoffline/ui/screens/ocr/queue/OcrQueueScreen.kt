package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.OcrQueueJob
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.SubScreenTopAppBar
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.enqueuechecker.OcrQueueEnqueueCheckerFloatingActionButton
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.preferences.OcrQueuePreferencesBottomSheet


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrQueueScreen(
    onNavigateUp: () -> Unit,
    viewModel: OcrQueueScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val queueStatus by viewModel.queueStatusUiState.collectAsStateWithLifecycle()
    val queueTaskCounts by viewModel.queueTaskCounts.collectAsStateWithLifecycle()

    val currentUiItems by viewModel.currentUiItems.collectAsStateWithLifecycle()
    val currentUiItemsLoading by viewModel.currentUiItemsLoading.collectAsStateWithLifecycle()

    val category by viewModel.currentScreenCategory.collectAsStateWithLifecycle()

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
            OcrQueueEnqueueCheckerFloatingActionButton(
                ocrQueueRunning = queueStatus.isRunning,
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
