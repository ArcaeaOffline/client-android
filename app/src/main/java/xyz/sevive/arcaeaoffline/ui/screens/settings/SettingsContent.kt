package xyz.sevive.arcaeaoffline.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.api.ArcaeaResourcesApiClient
import xyz.sevive.arcaeaoffline.data.IS_UNSTABLE_VERSION
import xyz.sevive.arcaeaoffline.ui.components.OperationTextItem
import xyz.sevive.arcaeaoffline.ui.components.SettingsGroupHeader
import xyz.sevive.arcaeaoffline.ui.components.SwitchItem
import xyz.sevive.arcaeaoffline.ui.components.TextItem
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogDismissTextButton
import xyz.sevive.arcaeaoffline.ui.navigation.LocalListDetailNavigationContext
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsSubScreen
import xyz.sevive.arcaeaoffline.ui.screens.NavEntryNavigateButton
import xyz.sevive.arcaeaoffline.ui.screens.settings.unstablealert.UnstableBuildAlertCard
import xyz.sevive.arcaeaoffline.ui.theme.spacing

@Composable
private fun ResourcesApiBaseUrlEditDialog(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(initialValue) }

    val trimmed = value.trim()
    val isValid = trimmed.startsWith("http://") || trimmed.startsWith("https://")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_general_pref_resources_api_base_url_dialog_title)) },
        text = {
            TextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                isError = value.isNotBlank() && !isValid,
                supportingText = {
                    if (value.isNotBlank() && !isValid) {
                        Text(stringResource(R.string.settings_general_pref_resources_api_base_url_invalid))
                    } else if (trimmed.startsWith("http://")) {
                        // Android 9+ silently blocks cleartext: an http URL is "valid" here, but
                        // every probe and download would fail with a generic network error.
                        Text(stringResource(R.string.settings_general_pref_resources_api_base_url_http_hint))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        dismissButton = {
            DialogDismissTextButton(
                onClick = {
                    onReset()
                    onDismiss()
                },
                customIcon = { Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null) },
                customLabel = { Text(stringResource(R.string.general_reset)) },
            )
        },
        confirmButton = {
            DialogConfirmButton(
                onClick = {
                    onConfirm(trimmed)
                    onDismiss()
                },
                enabled = isValid,
                customIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                customLabel = { Text(stringResource(R.string.general_save)) },
            )
        },
    )
}

@Composable
internal fun SettingsContent(
    onNavigateToEmergencyModeActivity: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val navContext = LocalListDetailNavigationContext.current
    val viewModel: SettingsViewModel = koinViewModel()
    val uiState by viewModel.appPreferencesUiState.collectAsStateWithLifecycle()
    val diagnosticsExportState by viewModel.diagnosticsExportState.collectAsStateWithLifecycle()
    val diagnosticsDirPicker =
        rememberDirectoryPickerLauncher { dir ->
            dir?.let(viewModel::exportDiagnostics)
        }

    var showBaseUrlEditDialog by rememberSaveable { mutableStateOf(false) }
    if (showBaseUrlEditDialog) {
        ResourcesApiBaseUrlEditDialog(
            initialValue = uiState.resourcesApiBaseUrl,
            onConfirm = viewModel::setResourcesApiBaseUrl,
            onReset = { viewModel.setResourcesApiBaseUrl(ArcaeaResourcesApiClient.DEFAULT_BASE_URL) },
            onDismiss = { showBaseUrlEditDialog = false },
        )
    }

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        if (IS_UNSTABLE_VERSION) {
            UnstableBuildAlertCard(
                onClick = { navContext.navigateToDetail(SettingsSubScreen.UnstableAlert.route) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.pagePadding),
                showDetails = true,
            )
        }

        SettingsGroupHeader(stringResource(R.string.settings_general_title))
        SwitchItem(
            value = uiState.autoSendCrashReports,
            onValueChange = viewModel::setAutoSendCrashReports,
            icon = Icons.Default.BugReport,
            title = stringResource(R.string.settings_app_pref_enable_sentry),
        )
        TextItem(
            onClick = { showBaseUrlEditDialog = true },
            leadingIcon = Icons.Default.CloudDownload,
            title = stringResource(R.string.settings_general_pref_resources_api_base_url),
            content = uiState.resourcesApiBaseUrl,
        )

        SettingsGroupHeader(stringResource(R.string.settings_debug_title))
        OperationTextItem(
            title = stringResource(R.string.diagnostics_export),
            state = diagnosticsExportState,
            leadingIcon = Icons.Default.BugReport,
            onClick = { diagnosticsDirPicker.launch() },
        )
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.error) {
            TextItem(
                title = stringResource(R.string.emergency_mode_title),
                leadingIcon = ImageVector.vectorResource(R.drawable.ic_activity_emergency_mode),
                leadingIconTint = LocalContentColor.current,
                trailingIcon = Icons.AutoMirrored.Default.ArrowForward,
                onClick = onNavigateToEmergencyModeActivity,
            )
        }

        HorizontalDivider()

        NavEntryNavigateButton(
            titleResId = SettingsSubScreen.About.title,
            icon = Icons.Outlined.Info,
        ) {
            navContext.navigateToDetail(SettingsSubScreen.About.route)
        }
    }
}
