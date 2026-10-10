package xyz.sevive.arcaeaoffline.ui.activities

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.dialogs.toAndroidUri
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.delay
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.context.persistUriPermissions
import xyz.sevive.arcaeaoffline.ui.components.OperationTextItem
import xyz.sevive.arcaeaoffline.ui.components.SettingsGroupHeader
import xyz.sevive.arcaeaoffline.ui.components.TextItem
import xyz.sevive.arcaeaoffline.ui.theme.spacing
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UiTopAppBar(modifier: Modifier = Modifier) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(stringResource(R.string.emergency_mode_title))
            }
        },
        navigationIcon = {
            Icon(
                painterResource(R.drawable.ic_activity_emergency_mode),
                contentDescription = null,
            )
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.error,
                titleContentColor = MaterialTheme.colorScheme.onError,
                navigationIconContentColor = MaterialTheme.colorScheme.onError,
            ),
    )
}

@Composable
private fun UiConfirmDeleteDialog(
    title: String,
    body: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                body?.let { Text(it) }
                Text(stringResource(R.string.emergency_mode_irreversible_warning))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors =
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Text(stringResource(R.string.general_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.general_cancel))
            }
        },
    )
}

@Composable
fun EmergencyModeActivityUi(
    modifier: Modifier = Modifier,
    viewModel: EmergencyModeActivityViewModel,
) {
    val context = LocalContext.current

    val outputDirectory by viewModel.outputDirectory.collectAsStateWithLifecycle()
    val outputDirectoryValid by viewModel.outputDirectoryValid.collectAsStateWithLifecycle()
    val isOcrQueueWorkRunning by viewModel.isOcrQueueWorkRunning.collectAsStateWithLifecycle()
    val isOcrDependenciesWorkRunning by viewModel.isOcrDependenciesWorkRunning.collectAsStateWithLifecycle()
    val ocrDependenciesDeleteState by viewModel.ocrDependenciesDeleteState.collectAsStateWithLifecycle()
    val ocrQueueDeleteState by viewModel.ocrQueueDeleteState.collectAsStateWithLifecycle()
    val ocrQueueClearState by viewModel.ocrQueueClearState.collectAsStateWithLifecycle()
    val databaseBackupState by viewModel.databaseBackupState.collectAsStateWithLifecycle()
    val diagnosticsExportState by viewModel.diagnosticsExportState.collectAsStateWithLifecycle()
    val restartRequired by viewModel.restartRequired.collectAsStateWithLifecycle()

    var showDeleteOcrDependenciesDialog by remember { mutableStateOf(false) }
    var showDeleteOcrQueueDbDialog by remember { mutableStateOf(false) }
    var showClearOcrQueueDialog by remember { mutableStateOf(false) }

    val dirPicker =
        rememberDirectoryPickerLauncher { dir ->
            dir?.let {
                context.persistUriPermissions(
                    it.toAndroidUri(),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
                viewModel.setOutputDirectory(it)
            }
        }

    LaunchedEffect(key1 = Unit) {
        viewModel.reloadPreferencesOnStartUp()
    }

    LaunchedEffect(restartRequired) {
        if (restartRequired) {
            // Give the result states a moment to be seen, then end the whole process
            delay(1.seconds)
            (context as? Activity)?.finishAffinity()
            exitProcess(0)
        }
    }

    Scaffold(modifier, topBar = { UiTopAppBar() }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .consumeWindowInsets(padding)
                .padding(
                    top = padding.calculateTopPadding() + MaterialTheme.spacing.sm,
                    bottom = padding.calculateBottomPadding() + MaterialTheme.spacing.sm,
                ),
        ) {
            SettingsGroupHeader(stringResource(R.string.emergency_mode_output_directory_title))
            TextItem(
                title = stringResource(R.string.emergency_mode_output_directory_select_item_title),
                content =
                    outputDirectory?.path
                        ?: stringResource(R.string.emergency_mode_output_directory_none),
                leadingSlot = {
                    Icon(Icons.Outlined.Folder, contentDescription = null)
                },
                trailingSlot = {
                    if (outputDirectory != null) {
                        when (outputDirectoryValid) {
                            null -> {
                                CircularProgressIndicator(
                                    Modifier.size(MaterialTheme.spacing.xl),
                                )
                            }

                            true -> {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription =
                                        stringResource(R.string.emergency_mode_output_directory_status_valid),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }

                            false -> {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription =
                                        stringResource(R.string.emergency_mode_output_directory_status_invalid),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                },
                onClick = { dirPicker.launch() },
            )

            SettingsGroupHeader(stringResource(R.string.emergency_mode_ocr_title))
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.error,
            ) {
                OperationTextItem(
                    title = stringResource(R.string.emergency_mode_clear_ocr_queue_button),
                    state = ocrQueueClearState,
                    content =
                        if (isOcrQueueWorkRunning) {
                            stringResource(R.string.emergency_mode_ocr_task_running)
                        } else {
                            null
                        },
                    leadingIcon = Icons.Default.DeleteOutline,
                    leadingIconTint = MaterialTheme.colorScheme.error,
                    onClick = { showClearOcrQueueDialog = true },
                )
                OperationTextItem(
                    title = stringResource(R.string.emergency_mode_delete_ocr_queue_db_button),
                    state = ocrQueueDeleteState,
                    content =
                        if (isOcrQueueWorkRunning) {
                            stringResource(R.string.emergency_mode_ocr_task_running)
                        } else {
                            null
                        },
                    leadingIcon = Icons.Default.DeleteForever,
                    leadingIconTint = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteOcrQueueDbDialog = true },
                )
                OperationTextItem(
                    title = stringResource(R.string.emergency_mode_ocr_delete_dependencies_button),
                    state = ocrDependenciesDeleteState,
                    content =
                        if (isOcrDependenciesWorkRunning) {
                            stringResource(R.string.emergency_mode_ocr_task_running)
                        } else {
                            null
                        },
                    leadingIcon = Icons.Default.DeleteForever,
                    leadingIconTint = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteOcrDependenciesDialog = true },
                )
            }

            SettingsGroupHeader(stringResource(R.string.emergency_mode_database_title))
            OperationTextItem(
                title = stringResource(R.string.emergency_mode_database_copy_item_title),
                state = databaseBackupState,
                leadingIcon = Icons.Default.FileCopy,
                enabled = outputDirectoryValid == true,
                onClick = { viewModel.copyDatabase() },
            )

            SettingsGroupHeader(stringResource(R.string.emergency_mode_diagnostics_title))
            OperationTextItem(
                title = stringResource(R.string.diagnostics_export),
                state = diagnosticsExportState,
                leadingIcon = Icons.Default.BugReport,
                enabled = outputDirectoryValid == true,
                onClick = { viewModel.exportDiagnostics() },
            )
        }
    }

    if (showClearOcrQueueDialog) {
        UiConfirmDeleteDialog(
            title = stringResource(R.string.emergency_mode_clear_ocr_queue_button),
            body = stringResource(R.string.emergency_mode_clear_ocr_queue_confirm_text),
            onConfirm = {
                showClearOcrQueueDialog = false
                viewModel.clearOcrQueue()
            },
            onDismiss = { showClearOcrQueueDialog = false },
        )
    }

    if (showDeleteOcrDependenciesDialog) {
        UiConfirmDeleteDialog(
            title = stringResource(R.string.emergency_mode_ocr_delete_dependencies_button),
            body =
                stringResource(
                    R.string.emergency_mode_ocr_delete_dependencies_confirm_text,
                    viewModel.ocrDependencyFileNames.joinToString(separator = "\n"),
                ),
            onConfirm = {
                showDeleteOcrDependenciesDialog = false
                viewModel.deleteAllOcrDependencies()
            },
            onDismiss = { showDeleteOcrDependenciesDialog = false },
        )
    }

    if (showDeleteOcrQueueDbDialog) {
        UiConfirmDeleteDialog(
            title = stringResource(R.string.emergency_mode_delete_ocr_queue_db_button),
            body = stringResource(R.string.emergency_mode_delete_ocr_queue_db_confirm_text),
            onConfirm = {
                showDeleteOcrQueueDbDialog = false
                viewModel.deleteOcrQueueDatabase()
            },
            onDismiss = { showDeleteOcrQueueDbDialog = false },
        )
    }
}
