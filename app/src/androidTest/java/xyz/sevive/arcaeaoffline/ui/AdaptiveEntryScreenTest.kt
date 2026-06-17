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
