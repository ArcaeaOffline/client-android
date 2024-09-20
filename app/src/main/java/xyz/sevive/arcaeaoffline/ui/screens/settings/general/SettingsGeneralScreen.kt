package xyz.sevive.arcaeaoffline.ui.screens.settings.general

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.preferences.SwitchPreferencesWidget
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
                SwitchPreferencesWidget(
                    value = uiState.enableSentry,
                    onValueChange = { onSetSentryEnabled(it) },
                    icon = Icons.Default.BugReport,
                    title = stringResource(R.string.settings_app_pref_enable_sentry),
                )
            }
        }
    }
}
