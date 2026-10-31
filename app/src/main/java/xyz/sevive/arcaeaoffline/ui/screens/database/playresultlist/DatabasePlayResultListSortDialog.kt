package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.BasicAlertDialogSurface
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.SelectItem
import xyz.sevive.arcaeaoffline.ui.components.SelectOption
import xyz.sevive.arcaeaoffline.ui.theme.spacing

@Composable
private fun DatabasePlayResultListSortDialogOrderSwitch(
    sortOrder: PlayResultSortOrder,
    onSortOrderChange: (PlayResultSortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        Modifier
            .clickable { onSortOrderChange(sortOrder.reverse()) }
            .padding(
                horizontal = MaterialTheme.spacing.lg,
                vertical = MaterialTheme.spacing.lg,
            ).then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconRow {
                Icon(painterResource(R.drawable.ic_sort_ascending), contentDescription = null)
                Text(stringResource(R.string.general_sort_ascending_short))
            }

            Switch(
                checked = sortOrder == PlayResultSortOrder.DESC,
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
    sortKey: PlayResultSortKey,
    onSortKeyChange: (PlayResultSortKey) -> Unit,
) {
    val options =
        listOf(
            SelectOption(PlayResultSortKey.TITLE, stringResource(R.string.general_song_title)),
            SelectOption(PlayResultSortKey.DIFFICULTY, stringResource(R.string.database_play_result_filter_difficulty)),
            SelectOption(PlayResultSortKey.SCORE, stringResource(R.string.arcaea_play_result_score)),
            SelectOption(PlayResultSortKey.PLAY_RATING, stringResource(R.string.arcaea_play_rating)),
            SelectOption(PlayResultSortKey.DATE, stringResource(R.string.datetime_picker_date)),
        )

    SelectItem(
        options = options,
        selected = sortKey,
        onSelect = onSortKeyChange,
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    )
}

@Composable
internal fun DatabasePlayResultListSortDialog(
    onDismissRequest: () -> Unit,
    sortOrder: PlayResultSortOrder,
    sortKey: PlayResultSortKey,
    onSortOrderChange: (PlayResultSortOrder) -> Unit,
    onSortKeyChange: (PlayResultSortKey) -> Unit,
) {
    BasicAlertDialogSurface(
        onDismissRequest = onDismissRequest,
    ) { paddingValues ->
        Column(
            Modifier.padding(top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Icon(painterResource(R.drawable.ic_sort), contentDescription = null)

            DatabasePlayResultListSortDialogOrderSwitch(
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange,
                Modifier.fillMaxWidth(),
            )

            DatabasePlayResultListSortDialogContent(
                sortKey = sortKey,
                onSortKeyChange = onSortKeyChange,
            )
        }
    }
}
