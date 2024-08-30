package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
    Column(modifier) {
        OcrQueueCategoryItem(
            onClick = { onSwitchScreen(OcrQueueScreenCategory.IDLE) },
            icon = Icons.Default.MoreHoriz,
            title = stringResource(R.string.ocr_queue_status_idle),
            count = idleCount,
            tint = MaterialTheme.colorScheme.onSurface,
        )

        OcrQueueCategoryItem(
            onClick = { onSwitchScreen(OcrQueueScreenCategory.PROCESSING) },
            icon = Icons.Default.HourglassBottom,
            title = stringResource(R.string.ocr_queue_status_processing),
            count = processingCount,
            tint = processingColor(),
        )

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

        OcrQueueCategoryItem(
            onClick = { onSwitchScreen(OcrQueueScreenCategory.ERROR) },
            icon = Icons.Default.Close,
            title = stringResource(R.string.ocr_queue_status_error),
            count = errorCount,
            tint = errorColor(),
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
) {
    BackHandler(category != OcrQueueScreenCategory.NULL) {
        onCategoryChange(OcrQueueScreenCategory.NULL)
    }

    @Composable
    fun OcrQueueListWrapper(uiItems: List<OcrQueueScreenViewModel.TaskUiItem>) {
        if (uiItems.isEmpty()) {
            EmptyScreen(Modifier.fillMaxSize())
            return
        }

        OcrQueueList(
            uiItems = uiItems,
            onSaveScore = onSavePlayResult,
            onDeleteTask = onDeleteTask,
            onEditPlayResult = onEditPlayResult
        )
    }

    AnimatedContent(
        targetState = category,
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
            )

            else -> {
                if (currentUiItemsLoading) {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                } else {
                    OcrQueueListWrapper(uiItems = currentUiItems)
                }
            }
        }
    }
}
