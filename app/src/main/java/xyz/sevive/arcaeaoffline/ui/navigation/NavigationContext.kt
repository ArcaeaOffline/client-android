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
 * [setDetailRoute] and [setExtraRoute] keep pane-specific route state
 * independent of the navigator's global `currentDestination`.
 * Without this separation, navigating the Extra pane would corrupt
 * the Detail pane's content key.
 *
 * Provided via [LocalListDetailNavigationContext] by `AdaptiveEntryScreen`.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ListDetailNavigationContext(
    private val navigator: ThreePaneScaffoldNavigator<String>,
    private val coroutineScope: CoroutineScope,
    private val setDetailRoute: (String?) -> Unit = {},
    private val setExtraRoute: (String?) -> Unit = {},
) {
    /**
     * Navigates back within the scaffold, then syncs both pane routes.
     * The Extra pane route is always cleared on back - navigating away
     * from Extra means its content should be dismissed.
     */
    fun navigateBack() {
        coroutineScope.launch {
            navigator.navigateBack()
            setDetailRoute(navigator.currentDestination?.contentKey)
            setExtraRoute(null)
        }
    }

    fun navigateToDetail(route: String) {
        setDetailRoute(route)
        setExtraRoute(null)
        coroutineScope.launch {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, route)
        }
    }

    fun navigateToExtra(route: String) {
        setExtraRoute(route)
        coroutineScope.launch {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Extra, route)
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
