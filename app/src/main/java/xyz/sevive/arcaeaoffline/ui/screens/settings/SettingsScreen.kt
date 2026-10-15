package xyz.sevive.arcaeaoffline.ui.screens.settings

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import xyz.sevive.arcaeaoffline.EmergencyModeActivity
import xyz.sevive.arcaeaoffline.ui.AdaptiveEntryScreen
import xyz.sevive.arcaeaoffline.ui.navigation.LocalListDetailNavigationContext
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsSubScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.about.SettingsAboutScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.aboutlibraries.SettingsAboutlibrariesScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.license.SettingsLicenseScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.unstablealert.SettingsUnstableAlertScreen

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

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
