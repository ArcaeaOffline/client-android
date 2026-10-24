package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R

@Composable
internal fun DatabasePlayResultListAppBarActions(
    inSelectMode: Boolean,
    selectedItemsCount: Int,
    activeFilterCount: Int,
    canSelectAll: Boolean,
    onShowFilterSheet: () -> Unit,
    onShowSortDialog: () -> Unit,
    onShowDeleteConfirmDialog: () -> Unit,
    onSelectAll: () -> Unit,
    onSelectedModeChange: (Boolean) -> Unit,
    onClearSelectedItems: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(targetState = inSelectMode, modifier = modifier, label = "selectModeActions") {
        Row {
            if (it) {
                IconButton(
                    onClick = onSelectAll,
                    enabled = canSelectAll,
                ) {
                    Icon(Icons.Default.SelectAll, stringResource(R.string.database_play_result_select_all))
                }

                IconButton(
                    onClick = onClearSelectedItems,
                    enabled = selectedItemsCount > 0,
                ) {
                    Icon(Icons.Default.Deselect, null)
                }

                IconButton(
                    onClick = onShowDeleteConfirmDialog,
                    enabled = selectedItemsCount > 0,
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                ) {
                    Icon(Icons.Default.Delete, null)
                }
            } else {
                IconButton(onClick = onShowFilterSheet) {
                    BadgedBox(
                        badge = {
                            if (activeFilterCount > 0) {
                                Badge { Text(activeFilterCount.toString()) }
                            }
                        },
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            stringResource(R.string.database_play_result_filter),
                        )
                    }
                }

                IconButton(onClick = onShowSortDialog) {
                    Icon(painterResource(R.drawable.ic_sort), null)
                }

                IconButton(onClick = { onSelectedModeChange(true) }) {
                    Icon(Icons.Default.Checklist, null)
                }
            }
        }
    }
}
