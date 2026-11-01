package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import xyz.sevive.arcaeaoffline.R

@Composable
internal fun <T> FilterOptionPickerPage(
    options: List<T>,
    selected: Set<T>,
    counts: Map<T, Int>,
    label: @Composable (T) -> String,
    onSelectionChange: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState()),
    ) {
        options.forEach { option ->
            val isSelected = option in selected
            val count = counts[option] ?: 0

            SelectableRow(
                title = label(option),
                trailing = pluralStringResource(R.plurals.database_play_result_entries, count, count),
                selected = isSelected,
                // A zero-count option cannot be added, but a selected one stays
                // enabled so it can be removed.
                enabled = isSelected || count > 0,
                onClick = {
                    onSelectionChange(
                        if (isSelected) selected - option else selected + option,
                    )
                },
            )
        }
    }
}
