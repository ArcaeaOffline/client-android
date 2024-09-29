package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButtonDefaults
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogDismissTextButton
import java.util.UUID


@Composable
internal fun DatabasePlayResultDeleteConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    selectedItemUuids: List<UUID>,
    modifier: Modifier = Modifier,
) {
    val selectedItemsCount = selectedItemUuids.size

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            DialogConfirmButton(
                onClick = onConfirm,
                colors = DialogConfirmButtonDefaults.dangerColors,
            )
        },
        modifier = modifier,
        dismissButton = {
            DialogDismissTextButton(onClick = onDismissRequest)
        },
        icon = {
            BadgedBox(badge = { Badge { Text(selectedItemsCount.toString()) } }) {
                Icon(Icons.Default.DeleteForever, null)
            }
        },
        title = { Text(stringResource(R.string.general_delete)) },
        text = {
            Text(
                pluralStringResource(
                    R.plurals.database_play_result_list_delete_confirm_dialog_content,
                    count = selectedItemsCount,
                    selectedItemsCount,
                )
            )
        },
        iconContentColor = MaterialTheme.colorScheme.error,
        titleContentColor = MaterialTheme.colorScheme.error,
    )
}
