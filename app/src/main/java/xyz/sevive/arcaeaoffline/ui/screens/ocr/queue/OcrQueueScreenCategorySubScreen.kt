package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.repositories.DatabaseReadiness
import xyz.sevive.arcaeaoffline.core.database.repositories.DatabaseReadinessSummary
import xyz.sevive.arcaeaoffline.ui.components.LoadingOverlay
import xyz.sevive.arcaeaoffline.ui.components.TextItem
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.tasklist.OcrQueueTaskList
import xyz.sevive.arcaeaoffline.ui.theme.extendedColorScheme

internal enum class OcrQueueScreenCategory {
    NULL,
    IDLE,
    PROCESSING,
    DONE,
    DONE_WITH_WARNING,
    ERROR,
}

@Composable
private fun NavigationSubScreen(
    onSwitchScreen: (OcrQueueScreenCategory) -> Unit,
    taskCounts: OcrQueueScreenViewModel.QueueTaskCounts,
    readinessSummary: DatabaseReadinessSummary?,
    onNavigateToResources: () -> Unit,
    onSaveAllTasks: () -> Unit,
    onStartSmartFix: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier) {
        item {
            OcrQueueCategoryItem(
                onClick = { onSwitchScreen(OcrQueueScreenCategory.IDLE) },
                icon = Icons.Default.MoreHoriz,
                title = stringResource(R.string.ocr_queue_status_idle),
                count = taskCounts.idle,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        item {
            OcrQueueCategoryItem(
                onClick = { onSwitchScreen(OcrQueueScreenCategory.PROCESSING) },
                icon = Icons.Default.HourglassBottom,
                title = stringResource(R.string.ocr_queue_status_processing),
                count = taskCounts.processing,
                tint = processingColor(),
            )
        }

        item {
            OcrQueueCategoryItem(
                onClick = { onSwitchScreen(OcrQueueScreenCategory.DONE) },
                icon = Icons.Default.Check,
                title = stringResource(R.string.ocr_queue_status_done),
                count = taskCounts.done,
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
                count = taskCounts.doneWithWarning,
                tint = MaterialTheme.extendedColorScheme.warning,
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
                count = taskCounts.error,
                tint = errorColor(),
            )
        }

        readinessSummary?.let { summary ->
            when (summary.readiness) {
                DatabaseReadiness.EMPTY, DatabaseReadiness.SONGS_ONLY -> {
                    item {
                        TextItem(
                            title = stringResource(R.string.ocr_guide_database_title),
                            content = stringResource(R.string.ocr_guide_database_content),
                            leadingIcon = Icons.Default.CloudDownload,
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = onNavigateToResources,
                        )
                    }
                }

                DatabaseReadiness.PARTIAL -> {
                    item {
                        TextItem(
                            title =
                                pluralStringResource(
                                    R.plurals.resources_missing_chart_info,
                                    summary.missingChartInfoCount,
                                    summary.missingChartInfoCount,
                                ),
                            content = stringResource(R.string.ocr_guide_chart_info_partial_content),
                            leadingIcon = Icons.Default.CloudDownload,
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = onNavigateToResources,
                        )
                    }
                }

                DatabaseReadiness.READY -> {}
            }
        }
    }
}

@Composable
private fun OcrQueueListWrapper(
    uiItems: List<OcrQueueScreenViewModel.TaskUiItem>,
    onSaveTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onEditChart: (Long, Difficulty) -> Unit,
    onEditPlayResult: (Long, PlayResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiItems.isEmpty()) {
        EmptyScreen(modifier)
    } else {
        OcrQueueTaskList(
            uiItems = uiItems,
            onSaveTask = onSaveTask,
            onDeleteTask = onDeleteTask,
            onEditChart = onEditChart,
            onEditPlayResult = onEditPlayResult,
            modifier = modifier,
        )
    }
}

@Composable
internal fun OcrQueueScreenCategorySubScreen(
    category: OcrQueueScreenCategory,
    onCategoryChange: (OcrQueueScreenCategory) -> Unit,
    taskUiItems: List<OcrQueueScreenViewModel.TaskUiItem>,
    taskUiItemsLoading: Boolean,
    taskCounts: OcrQueueScreenViewModel.QueueTaskCounts,
    readinessSummary: DatabaseReadinessSummary?,
    onNavigateToResources: () -> Unit,
    onSaveTask: (Long) -> Unit,
    onSaveAllTasks: () -> Unit,
    onStartSmartFix: () -> Unit,
    onDeleteTask: (Long) -> Unit,
    onEditChart: (Long, Difficulty) -> Unit,
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
            OcrQueueScreenCategory.NULL -> {
                NavigationSubScreen(
                    onSwitchScreen = { newCategory -> onCategoryChange(newCategory) },
                    taskCounts = taskCounts,
                    readinessSummary = readinessSummary,
                    onNavigateToResources = onNavigateToResources,
                    onSaveAllTasks = onSaveAllTasks,
                    onStartSmartFix = onStartSmartFix,
                    modifier = modifier,
                )
            }

            else -> {
                LoadingOverlay(loading = taskUiItemsLoading, modifier = modifier) {
                    OcrQueueListWrapper(
                        uiItems = taskUiItems,
                        onSaveTask = onSaveTask,
                        onDeleteTask = onDeleteTask,
                        onEditChart = onEditChart,
                        onEditPlayResult = onEditPlayResult,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}
