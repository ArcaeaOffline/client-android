package xyz.sevive.arcaeaoffline.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.ui.SettingsSwitch
import xyz.sevive.arcaeaoffline.R


@Composable
internal fun SettingsAppPreferences(
    uiState: SettingsViewModel.AppPreferencesUiState,
    onSetSentryEnabled: (Boolean) -> Unit
) {
    SettingsSwitch(
        state = uiState.enableSentry,
        title = { Text(stringResource(R.string.settings_app_pref_enable_sentry)) },
        icon = { Icon(Icons.Default.BugReport, contentDescription = null) },
        onCheckedChange = onSetSentryEnabled,
    )
}
