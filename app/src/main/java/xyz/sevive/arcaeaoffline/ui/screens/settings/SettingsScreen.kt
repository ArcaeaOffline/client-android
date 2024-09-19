package xyz.sevive.arcaeaoffline.ui.screens.settings

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.GeneralEntryScreen
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsScreenDestination
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.about.SettingsAboutScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.aboutlibraries.SettingsAboutlibrariesScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.general.SettingsGeneralScreen
import xyz.sevive.arcaeaoffline.ui.screens.settings.license.SettingsLicenseScreen


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun SettingsScreen(
    vm: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()

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
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                },
            )
        },
    ) { route ->
        when (route) {
            SettingsScreenDestination.General.route -> {
                SettingsGeneralScreen(
                    onNavigateUp = { onNavigateUp() },
                    uiState = generalUiState,
                    onSetSentryEnabled = { vm.setEnableSentry(it) },
                )
            }

            SettingsScreenDestination.About.route -> {
                SettingsAboutScreen(
                    onNavigateUp = { onNavigateUp() },
                    onNavigateToLicenseScreen = {
                        navigator.navigateTo(
                            ListDetailPaneScaffoldRole.Detail,
                            SettingsScreenDestination.License.route
                        )
                    },
                    onNavigateToAboutlibrariesScreen = {
                        navigator.navigateTo(
                            ListDetailPaneScaffoldRole.Detail,
                            SettingsScreenDestination.Aboutlibraries.route
                        )
                    },
                )
            }

            SettingsScreenDestination.License.route -> {
                SettingsLicenseScreen(onNavigateUp = { onNavigateUp() })
            }

            SettingsScreenDestination.Aboutlibraries.route -> {
                SettingsAboutlibrariesScreen(onNavigateUp = { onNavigateUp() })
            }

            else -> {
                EmptyScreen()
            }
        }
    }
}
