package xyz.sevive.arcaeaoffline.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.data.IS_UNSTABLE_VERSION
import xyz.sevive.arcaeaoffline.ui.components.ActionButton
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreenDestinations
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsScreenDestination


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsNavEntry(
    onNavigateToSubRoute: (String) -> Unit, modifier: Modifier = Modifier
) {
    Scaffold(
        modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(MainScreenDestinations.Settings.title)) })
        },
    ) {
        LazyColumn(
            Modifier.padding(it),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding_main_screen))
        ) {
            if (IS_UNSTABLE_VERSION) {
                item {
                    UnstableBuildAlert(Modifier.fillMaxWidth(), showDetails = true)
                }
            }

            item {
                ActionButton(
                    onClick = { onNavigateToSubRoute(SettingsScreenDestination.General.route) },
                    headSlot = { Icon(Icons.Default.Apps, contentDescription = null) },
                    title = stringResource(SettingsScreenDestination.General.title),
                )
            }

            item {
                ActionButton(
                    onClick = { onNavigateToSubRoute(SettingsScreenDestination.About.route) },
                    headSlot = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    title = stringResource(SettingsScreenDestination.About.title),
                )
            }
        }
    }
}
