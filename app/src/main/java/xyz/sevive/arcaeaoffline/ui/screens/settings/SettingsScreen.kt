package xyz.sevive.arcaeaoffline.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.EmergencyModeActivity
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.GeneralEntryScreen
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsScreenDestination
import xyz.sevive.arcaeaoffline.ui.screens.settings.about.SettingsAboutScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.aboutlibraries.SettingsAboutlibrariesScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.general.SettingsGeneralScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.license.SettingsLicenseScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.unstablealert.SettingsUnstableAlertScreen


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val coroutineScope = rememberCoroutineScope()

    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val onNavigateUp = remember {
        { backPressedDispatcher?.onBackPressed() }
    }

    val generalUiState by vm.appPreferencesUiState.collectAsStateWithLifecycle()

    GeneralEntryScreen(
        navigator = navigator,
        listPane = {
            SettingsNavEntry(
                onNavigateToSubRoute = {
                    coroutineScope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                    }
                },
                onNavigateToEmergencyModeActivity = {
                    context.startActivity(Intent(context, EmergencyModeActivity::class.java))
                },
            )
        },
    ) { route ->
        when (route) {
            SettingsScreenDestination.General.route -> {
                SettingsGeneralScreen(
                    onNavigateUp = { onNavigateUp() },
                    uiState = generalUiState,
                    onSetAutoSendCrashReports = { vm.setAutoSendCrashReports(it) },
                )
            }

            SettingsScreenDestination.About.route -> {
                SettingsAboutScreen(
                    onNavigateUp = { onNavigateUp() },
                    onNavigateToLicenseScreen = {
                        coroutineScope.launch {
                            navigator.navigateTo(
                                ListDetailPaneScaffoldRole.Detail,
                                SettingsScreenDestination.License.route
                            )
                        }
                    },
                    onNavigateToAboutlibrariesScreen = {
                        coroutineScope.launch {
                            navigator.navigateTo(
                                ListDetailPaneScaffoldRole.Detail,
                                SettingsScreenDestination.Aboutlibraries.route
                            )
                        }
                    },
                )
            }

            SettingsScreenDestination.License.route -> {
                SettingsLicenseScreen(onNavigateUp = { onNavigateUp() })
            }

            SettingsScreenDestination.Aboutlibraries.route -> {
                SettingsAboutlibrariesScreen(onNavigateUp = { onNavigateUp() })
            }

            SettingsScreenDestination.UnstableAlert.route -> {
                SettingsUnstableAlertScreen(onNavigateUp = { onNavigateUp() })
            }
        }
    }
}
