package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen

internal enum class OcrQueueScreenCategory {
    NULL, IDLE, PROCESSING, DONE, DONE_WITH_WARNING, ERROR
}

@Composable
private fun NavigationSubScreen(
    onSwitchScreen: (OcrQueueScreenCategory) -> Unit,
    idleCount: Int,
    processingCount: Int,
    doneCount: Int,
    doneWithWarningCount: Int,
    errorCount: Int,
    onSaveAllTasks: () -> Unit,
    onStartSmartFix: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier) {
        item {
            OcrQueueCategoryItem(
                onClick = { onSwitchScreen(OcrQueueScreenCategory.IDLE) },
                icon = Icons.Default.MoreHoriz,
                title = stringResource(R.string.ocr_queue_status_idle),
                count = idleCount,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        item {
            OcrQueueCategoryItem(
                onClick = { onSwitchScreen(OcrQueueScreenCategory.PROCESSING) },
                icon = Icons.Default.HourglassBottom,
                title = stringResource(R.string.ocr_queue_status_processing),
                count = processingCount,
                tint = processingColor(),
            )
        }

        item {
            OcrQueueCategoryItem(
                onClick = { onSwitchScreen(OcrQueueScreenCategory.DONE) },
                icon = Icons.Default.Check,
                title = stringResource(R.string.ocr_queue_status_done),
                count = doneCount,
                tint = doneColor(),
            ) {
                IconButton(onClick = onSaveAllTasks) {
                    Icon(Icons.Default.SaveAlt, contentDescription = null)
                }
            }
        }

        item {
            OcrQueueCategoryItem(
                onClick = { onSwitchScreen(OcrQueueScreenCategory.DONE_WITH_WARNING) },
                icon = Icons.Default.Warning,
                title = stringResource(R.string.ocr_queue_status_done_with_warning),
                count = doneWithWarningCount,
                tint = MaterialTheme.colorScheme.onSurface,
            ) {
                IconButton(onClick = { onStartSmartFix() }) {
                    Icon(Icons.Filled.Build, contentDescription = null)
                }
            }
        }

        item {
            OcrQueueCategoryItem(
                onClick = { onSwitchScreen(OcrQueueScreenCategory.ERROR) },
                icon = Icons.Default.Close,
                title = stringResource(R.string.ocr_queue_status_error),
                count = errorCount,
                tint = errorColor(),
            )
        }
    }
}

@Composable
private fun OcrQueueListWrapper(
    uiItems: List<OcrQueueScreenViewModel.TaskUiItem>,
    onSavePlayResult: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onEditPlayResult: (Long, PlayResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showEmptyScreen by remember {
        derivedStateOf { uiItems.isEmpty() }
    }

    if (showEmptyScreen) {
        EmptyScreen(modifier)
    } else {
        OcrQueueList(
            uiItems = uiItems,
            onSaveScore = onSavePlayResult,
            onDeleteTask = onDeleteTask,
            onEditPlayResult = onEditPlayResult,
            modifier = modifier,
        )
    }
}

@Composable
internal fun OcrQueueScreenCategorySubScreen(
    category: OcrQueueScreenCategory,
    onCategoryChange: (OcrQueueScreenCategory) -> Unit,
    currentUiItems: List<OcrQueueScreenViewModel.TaskUiItem>,
    currentUiItemsLoading: Boolean,
    idleCount: Int,
    processingCount: Int,
    doneCount: Int,
    doneWithWarningCount: Int,
    errorCount: Int,
    onSavePlayResult: (Long) -> Unit,
    onSaveAllTasks: () -> Unit,
    onStartSmartFix: () -> Unit,
    onDeleteTask: (Long) -> Unit,
    onEditPlayResult: (Long, PlayResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(category != OcrQueueScreenCategory.NULL) {
        onCategoryChange(OcrQueueScreenCategory.NULL)
    }

    AnimatedContent(
        targetState = category,
        modifier = modifier,
        transitionSpec = {
            val isSlideToSubScreen = targetState != OcrQueueScreenCategory.NULL

            (slideInHorizontally { if (isSlideToSubScreen) it else -it } + fadeIn())
                .togetherWith(slideOutHorizontally { if (isSlideToSubScreen) -it else it } + fadeOut())
                .using(SizeTransform(clip = false))
        },
        label = "subScreenSwitch",
    ) {
        when (it) {
            OcrQueueScreenCategory.NULL -> NavigationSubScreen(
                onSwitchScreen = { newCategory -> onCategoryChange(newCategory) },
                idleCount = idleCount,
                processingCount = processingCount,
                doneCount = doneCount,
                doneWithWarningCount = doneWithWarningCount,
                errorCount = errorCount,
                onSaveAllTasks = onSaveAllTasks,
                onStartSmartFix = onStartSmartFix,
                modifier = modifier,
            )

            else -> {
                if (currentUiItemsLoading) {
                    Box(modifier) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                } else {
                    OcrQueueListWrapper(
                        uiItems = currentUiItems,
                        onSavePlayResult = onSavePlayResult,
                        onDeleteTask = onDeleteTask,
                        onEditPlayResult = onEditPlayResult,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}
