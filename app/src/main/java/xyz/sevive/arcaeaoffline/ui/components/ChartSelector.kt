package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider

@Composable
fun ChartSelector(
    chart: Chart?,
    onChartChange: (Chart?) -> Unit,
    viewModel: ChartSelectorViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    LaunchedEffect(key1 = chart) {
        viewModel.launchedEffectSet(chart)
    }

    val coroutineScope = rememberCoroutineScope()

    val enabledRatingClasses by viewModel.enabledRatingClasses.collectAsStateWithLifecycle()
    val ratingDetails by viewModel.ratingDetails.collectAsStateWithLifecycle()

    val chartInViewModel by viewModel.chart.collectAsStateWithLifecycle()
    val song by viewModel.song.collectAsStateWithLifecycle()
    val ratingClass = chartInViewModel?.ratingClass

    LaunchedEffect(key1 = chartInViewModel) {
        onChartChange(chartInViewModel)
    }


    Column(
        Modifier.padding(dimensionResource(R.dimen.card_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
    ) {
        ArcaeaPackAndSongSelector(
            song = song,
            onSongChanged = {
                coroutineScope.launch { viewModel.setSong(it) }
            },
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_text_padding))
        ) {
            Icon(painterResource(R.drawable.ic_rating_class), contentDescription = null)

            RatingClassSelector(
                selectedRatingClass = ratingClass,
                onRatingClassChange = {
                    coroutineScope.launch { viewModel.setRatingClass(it) }
                },
                enabledRatingClasses = enabledRatingClasses,
                ratingDetails = ratingDetails,
            )
        }
    }
}
