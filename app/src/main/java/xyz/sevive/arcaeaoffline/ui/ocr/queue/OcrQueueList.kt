package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult


@Composable
fun OcrQueueList(
    uiItems: List<OcrQueueScreenViewModel.TaskUiItem>,
    onSaveScore: (Int) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onEditPlayResult: (Int, PlayResult) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))) {
        items(uiItems, key = { it.id }) { uiItem ->
            OcrQueueListItem(
                uiItem,
                onDeleteTask = onDeleteTask,
                onEditPlayResult = onEditPlayResult,
                onSavePlayResult = onSaveScore,
            )
        }
    }
}
