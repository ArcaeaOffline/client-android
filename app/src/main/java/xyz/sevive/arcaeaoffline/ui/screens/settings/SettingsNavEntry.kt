package xyz.sevive.arcaeaoffline.ui.screens.settings

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.data.IS_UNSTABLE_VERSION
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.navigation.LocalListDetailNavigationContext
import xyz.sevive.arcaeaoffline.ui.navigation.MainScreen
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsSubScreen
import xyz.sevive.arcaeaoffline.ui.screens.NavEntryNavigateButton
import xyz.sevive.arcaeaoffline.ui.screens.settings.unstablealert.UnstableBuildAlertCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsNavEntry(
    onNavigateToEmergencyModeActivity: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navContext = LocalListDetailNavigationContext.current

    Scaffold(
        modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(MainScreen.Settings.title)) })
        },
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .consumeWindowInsets(it),
            contentPadding = it,
        ) {
            if (IS_UNSTABLE_VERSION) {
                item {
                    UnstableBuildAlertCard(
                        onClick = { navContext.navigateToDetail(SettingsSubScreen.UnstableAlert.route) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimensionResource(R.dimen.page_padding)),
                        showDetails = true,
                    )
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = SettingsSubScreen.General.title,
                    icon = Icons.Default.Apps,
                ) {
                    navContext.navigateToDetail(SettingsSubScreen.General.route)
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = SettingsSubScreen.About.title,
                    icon = Icons.Outlined.Info,
                ) {
                    navContext.navigateToDetail(SettingsSubScreen.About.route)
                }
            }

            item {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.error,
                ) {
                    TextPreferencesWidget(
                        title = stringResource(R.string.emergency_mode_title),
                        leadingIcon = ImageVector.vectorResource(R.drawable.ic_activity_emergency_mode),
                        leadingIconTint = LocalContentColor.current,
                        trailingIcon = Icons.AutoMirrored.Default.ArrowForward,
                    ) {
                        onNavigateToEmergencyModeActivity()
                    }
                }
            }
        }
    }
}
