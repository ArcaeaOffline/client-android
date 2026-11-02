package xyz.sevive.arcaeaoffline.ui.screens.settings

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsNavEntry(
    onNavigateToEmergencyModeActivity: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.icon_desc_navigate_back))
                    }
                },
            )
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
