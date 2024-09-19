package xyz.sevive.arcaeaoffline.ui.screens.settings.general

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.ui.SettingsSwitch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsScreenDestination
import xyz.sevive.arcaeaoffline.ui.screens.settings.SettingsViewModel


@Composable
internal fun SettingsGeneralScreen(
    onNavigateUp: () -> Unit,
    uiState: SettingsViewModel.AppPreferencesUiState,
    onSetSentryEnabled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        title = stringResource(SettingsScreenDestination.General.title)
    ) {
        LazyColumn(modifier) {
            item {
                SettingsSwitch(
                    state = uiState.enableSentry,
                    title = { Text(stringResource(R.string.settings_app_pref_enable_sentry)) },
                    icon = { Icon(Icons.Default.BugReport, contentDescription = null) },
                    onCheckedChange = onSetSentryEnabled,
                )
            }
        }
    }
}
