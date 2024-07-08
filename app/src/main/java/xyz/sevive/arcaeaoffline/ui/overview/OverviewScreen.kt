package xyz.sevive.arcaeaoffline.ui.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters

@Composable
private fun B30R10Label(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding))) {
        Text(label, Modifier.alignByBaseline(), style = MaterialTheme.typography.headlineLarge)
        Text(value, Modifier.alignByBaseline(), style = MaterialTheme.typography.displayLarge)
    }
}

@Composable
fun OverviewScreen(
    modifier: Modifier = Modifier,
    viewModel: OverviewViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    // i remember this width size class was comparable in alpha?
    // if true, this should be something like if (widthSizeClass >= EXPANDED) 2 else 1
    val gridColumns = when (windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.EXPANDED -> SimpleGridCells.Fixed(2)
        else -> SimpleGridCells.Fixed(1)
    }

    LaunchedEffect(key1 = Unit) { viewModel.reload() }

    Column(
        modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.page_padding))
    ) {
        AnimatedVisibility(visible = uiState.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        Row(verticalAlignment = Alignment.Bottom) {
            VerticalGrid(columns = gridColumns, Modifier.weight(1f)) {
                B30R10Label(label = "B30", value = ArcaeaFormatters.potentialToText(uiState.b30))
                B30R10Label(label = "R10", value = ArcaeaFormatters.potentialToText(uiState.r10))
            }

            FilledIconButton(onClick = { viewModel.reload() }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }
    }
}
