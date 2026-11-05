package xyz.sevive.arcaeaoffline.ui.screens.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen
import xyz.sevive.arcaeaoffline.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToResources: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OverviewViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val readinessUiState by viewModel.readinessUiState.collectAsStateWithLifecycle()
    val latestPlays by viewModel.latestPlays.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_home)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        val twoPane =
            currentWindowAdaptiveInfo()
                .windowSizeClass
                .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

        // Setup state and potential on one side, the activity feed on the other.
        val statusPane: @Composable ColumnScope.() -> Unit = {
            OverviewReadinessCard(
                readinessUiState,
                onNavigateToResources = onNavigateToResources,
                Modifier.fillMaxWidth(),
            )

            OverviewPotentialCard(uiState)
        }

        val recentPane: @Composable ColumnScope.() -> Unit = {
            Text(
                stringResource(R.string.home_recent_header),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
            )

            if (latestPlays.isEmpty()) {
                EmptyScreen(
                    Modifier.fillMaxWidth(),
                    text = stringResource(R.string.database_no_play_results),
                )
            } else {
                latestPlays.forEach { playResult ->
                    key(playResult.uuid) {
                        ArcaeaPlayResultSummaryCard(
                            playResult,
                            uiState.scoringMode,
                            Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        if (twoPane) {
            // Each pane scrolls on its own: the long activity feed never drags
            // the short status pane along with it.
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = MaterialTheme.spacing.pagePadding,
                            bottom = MaterialTheme.spacing.pagePadding,
                        ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    content = statusPane,
                )

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            end = MaterialTheme.spacing.pagePadding,
                            bottom = MaterialTheme.spacing.pagePadding,
                        ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    content = recentPane,
                )
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        bottom = MaterialTheme.spacing.pagePadding,
                        start = MaterialTheme.spacing.pagePadding,
                        end = MaterialTheme.spacing.pagePadding,
                    ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                statusPane()
                recentPane()
            }
        }
    }
}
