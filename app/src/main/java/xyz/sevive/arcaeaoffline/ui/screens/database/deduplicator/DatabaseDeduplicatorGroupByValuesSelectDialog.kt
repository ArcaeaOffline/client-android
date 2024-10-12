package xyz.sevive.arcaeaoffline.ui.screens.database.deduplicator

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.preferences.CheckboxPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


private val groupByValueResIdMap = mapOf(
    GroupByValue.SCORE to R.string.arcaea_play_result_score,
    GroupByValue.PURE to R.string.arcaea_play_result_pure,
    GroupByValue.FAR to R.string.arcaea_play_result_far,
    GroupByValue.LOST to R.string.arcaea_play_result_lost,
    GroupByValue.MAX_RECALL to R.string.arcaea_play_result_max_recall,
    GroupByValue.DATE to R.string.datetime_picker_label,
    GroupByValue.CLEAR_TYPE to R.string.arcaea_play_result_clear_type,
    GroupByValue.MODIFIER to R.string.arcaea_play_result_modifier,
)

@Composable
private fun groupByValueTitle(value: GroupByValue): String {
    return stringResource(groupByValueResIdMap[value] ?: R.string.develop_placeholder)
}

@Composable
private fun DatabaseDeduplicatorGroupByValuesSelectDialogContent(
    values: Set<GroupByValue>,
    onValueChange: (GroupByValue, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    @Composable
    fun ValueWrapper(targetValue: GroupByValue) {
        CheckboxPreferencesWidget(
            value = values.contains(targetValue),
            onValueChange = { enabled -> onValueChange(targetValue, enabled) },
            title = groupByValueTitle(targetValue),
        )
    }

    LazyColumn(modifier) {
        item { ValueWrapper(GroupByValue.SCORE) }
        item { ValueWrapper(GroupByValue.PURE) }
        item { ValueWrapper(GroupByValue.FAR) }
        item { ValueWrapper(GroupByValue.LOST) }
        item { ValueWrapper(GroupByValue.MAX_RECALL) }
        item { ValueWrapper(GroupByValue.DATE) }
        item { ValueWrapper(GroupByValue.CLEAR_TYPE) }
        item { ValueWrapper(GroupByValue.MODIFIER) }
    }
}

@Composable
internal fun DatabaseDeduplicatorGroupByValuesSelectDialog(
    onDismissRequest: () -> Unit,
    values: Set<GroupByValue>,
    onValuesChange: (Set<GroupByValue>) -> Unit
) {
    var selectedValues by rememberSaveable { mutableStateOf(values) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogConfirmButton(
                onClick = {
                    onValuesChange(selectedValues)
                    onDismissRequest()
                },
            )
        },
        text = {
            DatabaseDeduplicatorGroupByValuesSelectDialogContent(
                values = selectedValues,
                onValueChange = { value, enabled ->
                    if (enabled) selectedValues += value else selectedValues -= value
                },
            )
        }
    )
}

@PreviewLightDark
@Composable
private fun DialogContentPreview() {
    var values by remember {
        mutableStateOf(setOf(GroupByValue.SCORE))
    }

    ArcaeaOfflineTheme {
        Surface {
            DatabaseDeduplicatorGroupByValuesSelectDialogContent(
                values = values,
                onValueChange = { value, enabled ->
                    if (enabled) values += value else values -= value
                },
            )
        }
    }
}
