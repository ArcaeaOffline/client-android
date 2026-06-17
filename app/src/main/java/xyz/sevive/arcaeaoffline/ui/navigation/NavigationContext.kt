package xyz.sevive.arcaeaoffline.ui.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Encapsulates navigation within a ListDetail scaffold,
 * amortizing the coroutine scope so callers don't need to
 * launch coroutines themselves.
 *
 * Provided via [LocalListDetailNavigationContext] by `AdaptiveEntryScreen`.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ListDetailNavigationContext(
    private val navigator: ThreePaneScaffoldNavigator<String>,
    private val coroutineScope: CoroutineScope,
) {
    fun navigateBack() {
        coroutineScope.launch { navigator.navigateBack() }
    }

    fun navigateToDetail(route: String) {
        coroutineScope.launch {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, route)
        }
    }
}

/**
 * CompositionLocal provided by `AdaptiveEntryScreen`.
 * Available to NavEntry composables and any sub-screen within a ListDetail hierarchy.
 * Reads outside that scope will throw.
 */
val LocalListDetailNavigationContext =
    staticCompositionLocalOf<ListDetailNavigationContext> {
        error("LocalListDetailNavigationContext not provided. Wrap with AdaptiveEntryScreen.")
    }
