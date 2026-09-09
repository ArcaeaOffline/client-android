package xyz.sevive.arcaeaoffline.ui.screens.settings.general

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.api.ArcaeaResourcesApiClient
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogDismissTextButton
import xyz.sevive.arcaeaoffline.ui.components.preferences.SwitchPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsSubScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.SettingsViewModel

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
internal fun SettingsGeneralScreen(
    uiState: SettingsViewModel.AppPreferencesUiState,
    onSetAutoSendCrashReports: (Boolean) -> Unit,
    onSetResourcesApiBaseUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showBaseUrlEditDialog by rememberSaveable { mutableStateOf(false) }
    if (showBaseUrlEditDialog) {
        ResourcesApiBaseUrlEditDialog(
            initialValue = uiState.resourcesApiBaseUrl,
            onConfirm = onSetResourcesApiBaseUrl,
            onReset = { onSetResourcesApiBaseUrl(ArcaeaResourcesApiClient.DEFAULT_BASE_URL) },
            onDismiss = { showBaseUrlEditDialog = false },
        )
    }

    SubScreenContainer(
        title = stringResource(SettingsSubScreen.General.title),
    ) {
        LazyColumn(modifier) {
            item {
                SwitchPreferencesWidget(
                    value = uiState.autoSendCrashReports,
                    onValueChange = { onSetAutoSendCrashReports(it) },
                    icon = Icons.Default.BugReport,
                    title = stringResource(R.string.settings_app_pref_enable_sentry),
                )
            }

            item {
                TextPreferencesWidget(
                    onClick = { showBaseUrlEditDialog = true },
                    leadingIcon = Icons.Default.CloudDownload,
                    title = stringResource(R.string.settings_general_pref_resources_api_base_url),
                    content = uiState.resourcesApiBaseUrl,
                )
            }
        }
    }
}
