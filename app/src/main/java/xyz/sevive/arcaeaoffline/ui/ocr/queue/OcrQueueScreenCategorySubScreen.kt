package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen

internal enum class OcrQueueScreenCategory {
    NULL, IDLE, PROCESSING, DONE, DONE_WITH_WARNING, ERROR
}

@Composable
internal fun rememberOcrQueueScreenCategory(): MutableState<OcrQueueScreenCategory> {
    return rememberSaveable { mutableStateOf(OcrQueueScreenCategory.NULL) }
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
        )

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
    idleUiItems: List<OcrQueueScreenViewModel.TaskUiItem>,
    processingUiItems: List<OcrQueueScreenViewModel.TaskUiItem>,
    doneUiItems: List<OcrQueueScreenViewModel.TaskUiItem>,
    doneWithWarningUiItems: List<OcrQueueScreenViewModel.TaskUiItem>,
    errorUiItems: List<OcrQueueScreenViewModel.TaskUiItem>,
    onSavePlayResult: (Int) -> Unit,
    onSaveAllTasks: () -> Unit,
    onDeleteTask: (Int) -> Unit,
    onEditPlayResult: (Int, PlayResult) -> Unit,
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
                idleCount = idleUiItems.size,
                processingCount = processingUiItems.size,
                doneCount = doneUiItems.size,
                doneWithWarningCount = doneWithWarningUiItems.size,
                errorCount = errorUiItems.size,
                onSaveAllTasks = onSaveAllTasks,
            )

            OcrQueueScreenCategory.IDLE -> {
                OcrQueueListWrapper(uiItems = idleUiItems)
            }

            OcrQueueScreenCategory.PROCESSING -> {
                OcrQueueListWrapper(uiItems = processingUiItems)
            }

            OcrQueueScreenCategory.DONE -> {
                OcrQueueListWrapper(uiItems = doneUiItems)
            }

            OcrQueueScreenCategory.DONE_WITH_WARNING -> {
                OcrQueueListWrapper(uiItems = doneWithWarningUiItems)
            }

            OcrQueueScreenCategory.ERROR -> {
                OcrQueueListWrapper(uiItems = errorUiItems)
            }
        }
    }
}
