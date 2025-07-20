package xyz.sevive.arcaeaoffline.ui.screens.ocr

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.ui.GeneralEntryScreen
import xyz.sevive.arcaeaoffline.ui.navigation.OcrScreenDestinations
import xyz.sevive.arcaeaoffline.ui.screens.ocr.dependencies.OcrDependenciesScreen
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.OcrQueueScreen


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun OcrEntryScreen() {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val coroutineScope = rememberCoroutineScope()

    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    GeneralEntryScreen(
        navigator = navigator,
        listPane = {
            OcrNavEntry(onNavigateToSubRoute = {
                coroutineScope.launch {
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                }
            })
        },
    ) {
        when (it) {
            OcrScreenDestinations.Dependencies.route -> {
                OcrDependenciesScreen(onNavigateUp = { backPressedDispatcher?.onBackPressed() })
            }

            OcrScreenDestinations.Queue.route -> {
                OcrQueueScreen(onNavigateUp = { backPressedDispatcher?.onBackPressed() })
            }
        }
    }
}
