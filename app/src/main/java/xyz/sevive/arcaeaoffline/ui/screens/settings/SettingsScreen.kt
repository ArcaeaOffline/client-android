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
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsSubScreen
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
                SettingsSubScreen.General.route -> {
                    SettingsGeneralScreen(
                        uiState = generalUiState,
                        onSetAutoSendCrashReports = { vm.setAutoSendCrashReports(it) },
                    )
                }

                SettingsSubScreen.About.route -> {
                    SettingsAboutScreen(
                        onNavigateToLicenseScreen = {
                            navContext.navigateToExtra(SettingsSubScreen.License.route)
                        },
                        onNavigateToAboutlibrariesScreen = {
                            navContext.navigateToExtra(SettingsSubScreen.Aboutlibraries.route)
                        },
                    )
                }

                SettingsSubScreen.UnstableAlert.route -> {
                    SettingsUnstableAlertScreen()
                }
            }
        },
        extraPane = { route ->
            when (route) {
                SettingsSubScreen.License.route -> SettingsLicenseScreen()
                SettingsSubScreen.Aboutlibraries.route -> SettingsAboutlibrariesScreen()
            }
        },
    )
}
