package xyz.sevive.arcaeaoffline.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters

@Composable
fun B30R10Label(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding))) {
        Text(label, Modifier.alignByBaseline(), style = MaterialTheme.typography.headlineLarge)
        Text(value, Modifier.alignByBaseline(), style = MaterialTheme.typography.displayLarge)
    }
}

@Composable
fun OverviewScreen(
    modifier: Modifier = Modifier,
    overviewModel: OverviewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val b30 by overviewModel.b30.collectAsState()

    Column(
        modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.page_padding))
    ) {
        LazyVerticalGrid(GridCells.Adaptive(200.dp)) {
            item {
                B30R10Label(label = "B30", value = ArcaeaFormatters.potentialToText(b30))
            }

            item {
                B30R10Label(label = "R10", value = "-.--")
            }
        }
    }
}
