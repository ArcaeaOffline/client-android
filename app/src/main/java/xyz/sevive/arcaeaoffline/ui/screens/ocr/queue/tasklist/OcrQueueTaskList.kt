package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.tasklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.OcrQueueScreenViewModel


@Composable
internal fun OcrQueueTaskList(
    uiItems: List<OcrQueueScreenViewModel.TaskUiItem>,
    onSaveTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onEditChart: (Long, Chart) -> Unit,
    onEditPlayResult: (Long, PlayResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier.padding(horizontal = dimensionResource(R.dimen.page_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
    ) {
        items(uiItems, key = { it.id }) {
            OcrQueueTaskListItem(
                uiItem = it,
                onDelete = { onDeleteTask(it.id) },
                onEditChart = { chart -> onEditChart(it.id, chart) },
                onEditPlayResult = { playResult -> onEditPlayResult(it.id, playResult) },
                onSaveTask = { onSaveTask(it.id) },
            )
        }
    }
}
