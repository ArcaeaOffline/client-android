package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.validation.PlayResultFinding
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogDismissTextButton
import xyz.sevive.arcaeaoffline.ui.components.playresult.PlayResultWarningDetails

/** Asks for confirmation before saving a play result that raised validator warnings. */
@Composable
fun SaveWithWarningsDialog(
    warnings: List<PlayResultFinding>,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.play_result_save_with_warnings_title)) },
        text = { PlayResultWarningDetails(warnings) },
        confirmButton = {
            DialogConfirmButton(
                onClick = onConfirm,
                customLabel = { Text(stringResource(R.string.general_save)) },
            )
        },
        dismissButton = { DialogDismissTextButton(onClick = onDismissRequest) },
    )
}
