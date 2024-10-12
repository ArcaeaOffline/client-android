package xyz.sevive.arcaeaoffline.ui.screens.database.deduplicator

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButtonDefaults
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogDismissTextButton
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.theme.extendedColorScheme


@Composable
private fun AutoMergeConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Default.Merge, contentDescription = null) },
        iconContentColor = MaterialTheme.extendedColorScheme.warning,
        title = { Text(stringResource(R.string.database_deduplicator_auto_merge)) },
        titleContentColor = MaterialTheme.extendedColorScheme.warning,
        text = { Text(stringResource(R.string.database_deduplicator_auto_merge_warning)) },
        dismissButton = { DialogDismissTextButton(onClick = onDismissRequest) },
        confirmButton = {
            DialogConfirmButton(
                onClick = {
                    onConfirm()
                    onDismissRequest()
                },
                colors = DialogConfirmButtonDefaults.warningColors,
            )
        },
    )
}

@Composable
internal fun DatabaseDeduplicatorWizardDialog(
    onDismissRequest: () -> Unit,
    onAutoSelect: (AutoSelectMode) -> Unit,
    onAutoMerge: () -> Unit,
) {
    var showAutoMergeConfirmDialog by rememberSaveable { mutableStateOf(false) }
    if (showAutoMergeConfirmDialog) {
        AutoMergeConfirmDialog(
            onDismissRequest = { showAutoMergeConfirmDialog = false },
            onConfirm = onAutoMerge,
        )
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null) },
        text = {
            LazyColumn {
                item {
                    TextPreferencesWidget(
                        onClick = { onAutoSelect(AutoSelectMode.IDENTICAL) },
                        title = stringResource(R.string.database_deduplicator_auto_select_identical),
                        content = stringResource(R.string.database_deduplicator_description_auto_select_identical),
                    )
                }

                item {
                    TextPreferencesWidget(
                        onClick = { onAutoSelect(AutoSelectMode.PROPERTIES_PRIORITY) },
                        title = stringResource(R.string.database_deduplicator_auto_select_properties_priority),
                        content = stringResource(R.string.database_deduplicator_description_auto_select_properties_priority),
                    )
                }

                item {
                    TextPreferencesWidget(
                        onClick = { onAutoSelect(AutoSelectMode.R30_PRIORITY) },
                        title = stringResource(R.string.database_deduplicator_auto_select_r30_priority),
                        content = stringResource(R.string.database_deduplicator_description_auto_select_r30_priority),
                    )
                }

                item {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.extendedColorScheme.warning
                    ) {
                        TextPreferencesWidget(
                            onClick = { showAutoMergeConfirmDialog = true },
                            leadingIcon = Icons.Default.Merge,
                            leadingIconTint = LocalContentColor.current,
                            title = stringResource(R.string.database_deduplicator_auto_merge),
                        )
                    }
                }
            }
        },
    )
}
