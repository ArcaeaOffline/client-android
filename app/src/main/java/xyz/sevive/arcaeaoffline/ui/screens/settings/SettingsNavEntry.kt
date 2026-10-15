package xyz.sevive.arcaeaoffline.ui.screens.settings

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsNavEntry(
    onNavigateToEmergencyModeActivity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(MainScreen.Settings.title)) })
        },
    ) { padding ->
        SettingsContent(
            onNavigateToEmergencyModeActivity = onNavigateToEmergencyModeActivity,
            modifier =
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(padding),
            contentPadding = padding,
        )
    }
}
