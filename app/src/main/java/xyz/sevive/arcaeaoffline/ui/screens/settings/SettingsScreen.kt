package xyz.sevive.arcaeaoffline.ui.screens.settings

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.EmergencyModeActivity
import xyz.sevive.arcaeaoffline.ui.AdaptiveEntryScreen
import xyz.sevive.arcaeaoffline.ui.navigation.LocalListDetailNavigationContext
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsScreenDestination
import xyz.sevive.arcaeaoffline.ui.screens.settings.about.SettingsAboutScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.aboutlibraries.SettingsAboutlibrariesScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.general.SettingsGeneralScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.license.SettingsLicenseScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.unstablealert.SettingsUnstableAlertScreen

@Composable
fun SettingsScreen(vm: SettingsViewModel = koinViewModel()) {
    val context = LocalContext.current
    val generalUiState by vm.appPreferencesUiState.collectAsStateWithLifecycle()

    AdaptiveEntryScreen(
        listPane = {
            SettingsNavEntry(
                onNavigateToEmergencyModeActivity = {
                    context.startActivity(Intent(context, EmergencyModeActivity::class.java))
                },
            )
        },
        detailPane = { route ->
            val navContext = LocalListDetailNavigationContext.current
            when (route) {
                SettingsScreenDestination.General.route -> {
                    SettingsGeneralScreen(
                        uiState = generalUiState,
                        onSetAutoSendCrashReports = { vm.setAutoSendCrashReports(it) },
                    )
                }

                SettingsScreenDestination.About.route -> {
                    SettingsAboutScreen(
                        onNavigateToLicenseScreen = {
                            navContext.navigateToExtra(SettingsScreenDestination.License.route)
                        },
                        onNavigateToAboutlibrariesScreen = {
                            navContext.navigateToExtra(SettingsScreenDestination.Aboutlibraries.route)
                        },
                    )
                }

                SettingsScreenDestination.UnstableAlert.route -> SettingsUnstableAlertScreen()
            }
        },
        extraPane = { route ->
            when (route) {
                SettingsScreenDestination.License.route -> SettingsLicenseScreen()
                SettingsScreenDestination.Aboutlibraries.route -> SettingsAboutlibrariesScreen()
            }
        },
    )
}
