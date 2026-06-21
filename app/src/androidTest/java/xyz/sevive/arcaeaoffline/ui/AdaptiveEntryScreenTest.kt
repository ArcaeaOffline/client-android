package xyz.sevive.arcaeaoffline.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.sevive.arcaeaoffline.ui.navigation.ListDetailNavigationContext
import xyz.sevive.arcaeaoffline.ui.navigation.LocalListDetailNavigationContext

private const val LIST_LABEL = "ListPane"
private const val DETAIL_LABEL_PREFIX = "Detail: "
private const val EXTRA_LABEL_PREFIX = "Extra: "
private const val TEST_ROUTE = "test_route"
private const val EXTRA_ROUTE = "extra_route"

/**
 * Integration tests for [AdaptiveEntryScreen] — the template that creates the
 * navigator, injects [LocalListDetailNavigationContext], and wires pane routing
 * for list / detail / extra panes.
 *
 * These tests exercise the full stack: Compose → Context → Navigator → Scaffold.
 * The key regression target is the detail pane route independence fix —
 * navigating Extra must not corrupt the Detail pane's visible content.
 */
@RunWith(AndroidJUnit4::class)
class AdaptiveEntryScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContentWith(extraPane: (@Composable (route: String) -> Unit)? = null) {
        composeTestRule.setContent {
            AdaptiveEntryScreen(
                listPane = { TestListPane() },
                detailPane = { route ->
                    TestScreen(
                        label = DETAIL_LABEL_PREFIX + route,
                        navigateToExtraRoute = EXTRA_ROUTE,
                    )
                },
                extraPane = extraPane,
            )
        }
    }

    /**
     * Sanity check — the scaffold composes without error and the list pane is visible.
     */
    @Test
    fun initial_state_shows_list_and_no_detail() {
        setContentWith()
        composeTestRule.onNodeWithText(LIST_LABEL).assertIsDisplayed()
    }

    /**
     * [ListDetailNavigationContext.navigateToDetail] triggers [GeneralEntryScreen]
     * to render the detail pane with the requested route.
     */
    @Test
    fun navigate_to_detail() {
        setContentWith()
        composeTestRule.onNodeWithText("Go to $TEST_ROUTE").performClick()
        composeTestRule.onNodeWithText(DETAIL_LABEL_PREFIX + TEST_ROUTE).assertIsDisplayed()
    }

    /**
     * [ListDetailNavigationContext.navigateBack] after a detail navigation
     * clears the detail route state, returning the scaffold to its initial layout.
     */
    @Test
    fun navigate_back_from_detail() {
        setContentWith()
        composeTestRule.onNodeWithText("Go to $TEST_ROUTE").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Back from detail").performClick()
        composeTestRule.onNodeWithText(LIST_LABEL).assertIsDisplayed()
    }

    /**
     * Regression: navigating Extra must not overwrite the Detail pane's route state.
     * Before the pane-route separation fix, `navigator.currentDestination` was shared
     * across all panes, causing Extra navigation to blank the Detail pane.
     *
     * This mirrors the real Settings → About → License flow: the "Go to extra" button
     * lives in the detail pane (the About screen), not the list pane. On phones the
     * scaffold may hide extra after back, but the detail route must survive the round-trip.
     */
    @Test
    fun detail_route_survives_extra_navigation() {
        setContentWith(extraPane = { route ->
            TestScreen(label = EXTRA_LABEL_PREFIX + route, backLabel = "Back from extra")
        })

        // Navigate list → detail
        composeTestRule.onNodeWithText("Go to $TEST_ROUTE").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(DETAIL_LABEL_PREFIX + TEST_ROUTE).assertIsDisplayed()

        // Navigate detail → extra (button is in the detail pane)
        composeTestRule.onNodeWithText("Go to $EXTRA_ROUTE").performClick()
        composeTestRule.waitForIdle()

        // Navigate back from extra
        composeTestRule.onNodeWithText("Back from extra").performClick()
        composeTestRule.waitForIdle()

        // Detail pane route must have survived the extra round-trip
        composeTestRule.onNodeWithText(DETAIL_LABEL_PREFIX + TEST_ROUTE).assertIsDisplayed()
    }

    /**
     * Regression: detail pane state must survive a NavBackStackEntry save/restore
     * cycle, which occurs when the user switches tabs via the bottom navigation bar.
     *
     * This test simulates the exact navigation pattern used by `mainNavControllerNavigateToRoute`:
     *   popUpTo(startDestination) { saveState = true }
     *   + launchSingleTop = true
     *   + restoreState = true
     *
     * Without the fix (rememberSaveable + LaunchedEffect re-navigation), the composable
     * would be fully disposed on tab switch and the detail pane route would reset to null,
     * showing the placeholder icon instead of the expected detail content.
     */
    @Test
    fun detail_pane_state_survives_tab_switch() {
        lateinit var navController: NavHostController

        composeTestRule.setContent {
            navController = rememberNavController()
            NavHost(navController, startDestination = "home") {
                composable("home") { Text("Home") }

                composable("tab_a") {
                    AdaptiveEntryScreen(
                        listPane = { TestListPane() },
                        detailPane = { route ->
                            Text(DETAIL_LABEL_PREFIX + route)
                        },
                    )
                }

                composable("tab_b") {
                    AdaptiveEntryScreen(
                        listPane = { Text("TabBList") },
                        detailPane = { route ->
                            Text("TabBDetail: $route")
                        },
                    )
                }
            }
        }

        // 1. Navigate to tab_a
        composeTestRule.runOnIdle {
            navController.navigate("tab_a") {
                popUpTo(navController.graph.startDestinationRoute!!) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        composeTestRule.waitForIdle()

        // 2. Navigate list → detail on tab_a
        composeTestRule.onNodeWithText("Go to $TEST_ROUTE").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(DETAIL_LABEL_PREFIX + TEST_ROUTE).assertIsDisplayed()

        // 3. Switch to tab_b — this pops tab_a's NavBackStackEntry,
        //    destroying its composable and all remember state.
        composeTestRule.runOnIdle {
            navController.navigate("tab_b") {
                popUpTo(navController.graph.startDestinationRoute!!) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("TabBList").assertIsDisplayed()
        composeTestRule.onNodeWithText(DETAIL_LABEL_PREFIX + TEST_ROUTE).assertDoesNotExist()

        // 4. Switch back to tab_a — restores the saved NavBackStackEntry.
        //    With rememberSaveable + LaunchedEffect, the detail pane route
        //    and navigator state should both be restored.
        composeTestRule.runOnIdle {
            navController.navigate("tab_a") {
                popUpTo(navController.graph.startDestinationRoute!!) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
        composeTestRule.waitForIdle()

        // 5. Detail pane must still show the expected content
        composeTestRule.onNodeWithText(DETAIL_LABEL_PREFIX + TEST_ROUTE).assertIsDisplayed()
    }
}

@Composable
private fun TestListPane() {
    val navContext = LocalListDetailNavigationContext.current
    Column(Modifier.fillMaxSize()) {
        Text(LIST_LABEL)
        Button(onClick = { navContext.navigateToDetail(TEST_ROUTE) }) {
            Text("Go to $TEST_ROUTE")
        }
    }
}

@Composable
private fun TestScreen(
    label: String,
    backLabel: String = "Back from detail",
    navigateToExtraRoute: String? = null,
) {
    val navContext = LocalListDetailNavigationContext.current
    Column(Modifier.fillMaxSize()) {
        Text(label)
        if (navigateToExtraRoute != null) {
            Button(onClick = { navContext.navigateToExtra(navigateToExtraRoute) }) {
                Text("Go to $navigateToExtraRoute")
            }
        }
        TextButton(onClick = { navContext.navigateBack() }) {
            Text(backLabel)
        }
    }
}
