package xyz.sevive.arcaeaoffline.ui.screens.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.R
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
            OverviewReadinessCard(
                readinessUiState,
                onNavigateToResources = onNavigateToResources,
                Modifier.fillMaxWidth(),
            )

            OverviewPotentialCard(uiState)

            if (latestPlays.isNotEmpty()) {
                Text(
                    stringResource(R.string.home_recent_header),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
                )

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
    }
}
