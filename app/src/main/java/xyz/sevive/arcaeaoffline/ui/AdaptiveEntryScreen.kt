package xyz.sevive.arcaeaoffline.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import xyz.sevive.arcaeaoffline.ui.navigation.ListDetailNavigationContext
import xyz.sevive.arcaeaoffline.ui.navigation.LocalListDetailNavigationContext

/**
 * Template for tab entry screens.
 * Creates the [androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator] and coroutine scope,
 * provides [LocalListDetailNavigationContext], and delegates layout to [GeneralEntryScreen].
 *
 * [listPane] and [detailPane] can read navigation operations
 * from [LocalListDetailNavigationContext] without threading parameters.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptiveEntryScreen(
    listPane: @Composable () -> Unit,
    detailPane: @Composable (route: String) -> Unit,
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val coroutineScope = rememberCoroutineScope()
    val navContext = remember { ListDetailNavigationContext(navigator, coroutineScope) }

    CompositionLocalProvider(LocalListDetailNavigationContext provides navContext) {
        GeneralEntryScreen(
            navigator = navigator,
            listPane = { listPane() },
            detailPane = { route -> detailPane(route) },
        )
    }
}
