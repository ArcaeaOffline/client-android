package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.preferences.BasePreferencesWidget
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
private fun DatabasePlayResultListSortDialogOrderSwitch(
    sortOrder: DatabasePlayResultListViewModel.SortOrder,
    onSortOrderChange: (DatabasePlayResultListViewModel.SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        Modifier
            .clickable { onSortOrderChange(sortOrder.reverse()) }
            .padding(
                horizontal = dimensionResource(R.dimen.pref_widget_horizontal_padding),
                vertical = dimensionResource(R.dimen.pref_widget_vertical_padding),
            )
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconRow {
                Icon(painterResource(R.drawable.ic_sort_ascending), contentDescription = null)
                Text(stringResource(R.string.general_sort_ascending_short))
            }

            Switch(
                checked = sortOrder == DatabasePlayResultListViewModel.SortOrder.DESC,
                onCheckedChange = null,
            )

            IconRow {
                Text(stringResource(R.string.general_sort_descending_short))
                Icon(painterResource(R.drawable.ic_sort_descending), contentDescription = null)
            }
        }
    }
}

@Composable
private fun DatabasePlayResultListSortDialogContent(
    sortOrder: DatabasePlayResultListViewModel.SortOrder,
    sortByValue: DatabasePlayResultListViewModel.SortByValue,
    onSortOrderChange: (DatabasePlayResultListViewModel.SortOrder) -> Unit,
    onSortByValueChange: (DatabasePlayResultListViewModel.SortByValue) -> Unit,
) {
    @Composable
    fun SortByValueWidgetWrapper(
        label: String,
        targetSortByValue: DatabasePlayResultListViewModel.SortByValue,
        modifier: Modifier = Modifier,
    ) {
        BasePreferencesWidget(
            title = { Text(label) },
            leadingSlot = {
                RadioButton(selected = sortByValue == targetSortByValue, onClick = null)
            },
            onClick = { onSortByValueChange(targetSortByValue) },
            modifier = modifier,
        )
    }

    LazyColumn {
        item {
            DatabasePlayResultListSortDialogOrderSwitch(
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange,
                Modifier.fillMaxWidth(),
            )
        }

        item {
            SortByValueWidgetWrapper(
                label = "ID",
                targetSortByValue = DatabasePlayResultListViewModel.SortByValue.ID,
                Modifier.fillMaxWidth(),
            )
        }

        item {
            SortByValueWidgetWrapper(
                label = stringResource(R.string.arcaea_play_result_score),
                targetSortByValue = DatabasePlayResultListViewModel.SortByValue.SCORE,
                Modifier.fillMaxWidth(),
            )
        }

        item {
            SortByValueWidgetWrapper(
                label = stringResource(R.string.arcaea_potential),
                targetSortByValue = DatabasePlayResultListViewModel.SortByValue.POTENTIAL,
                Modifier.fillMaxWidth(),
            )
        }

        item {
            SortByValueWidgetWrapper(
                label = stringResource(R.string.datetime_picker_label),
                targetSortByValue = DatabasePlayResultListViewModel.SortByValue.DATE,
                Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun DatabasePlayResultListSortDialog(
    onDismissRequest: () -> Unit,
    sortOrder: DatabasePlayResultListViewModel.SortOrder,
    sortByValue: DatabasePlayResultListViewModel.SortByValue,
    onSortOrderChange: (DatabasePlayResultListViewModel.SortOrder) -> Unit,
    onSortByValueChange: (DatabasePlayResultListViewModel.SortByValue) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(painterResource(R.drawable.ic_sort), contentDescription = null) },
        text = {
            DatabasePlayResultListSortDialogContent(
                sortOrder = sortOrder,
                sortByValue = sortByValue,
                onSortOrderChange = onSortOrderChange,
                onSortByValueChange = onSortByValueChange,
            )
        },
        confirmButton = {},
    )
}

@PreviewLightDark
@Composable
private fun DatabasePlayResultListSortDialogContentPreview() {
    var sortOrder by rememberSaveable {
        mutableStateOf(DatabasePlayResultListViewModel.SortOrder.ASC)
    }
    var sortByValue by rememberSaveable {
        mutableStateOf(DatabasePlayResultListViewModel.SortByValue.POTENTIAL)
    }

    ArcaeaOfflineTheme {
        Surface {
            DatabasePlayResultListSortDialogContent(
                sortOrder = sortOrder,
                sortByValue = sortByValue,
                onSortOrderChange = { sortOrder = it },
                onSortByValueChange = { sortByValue = it },
            )
        }
    }
}
