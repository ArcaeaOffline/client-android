package xyz.sevive.arcaeaoffline.ui.screens.utilities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.firstOrNull
import org.koin.compose.koinInject
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPackAndSongQuickSearch
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaRatingClassSelector
import xyz.sevive.arcaeaoffline.ui.components.PlayRatingCalculator
import xyz.sevive.arcaeaoffline.ui.navigation.UtilitiesSubScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilitiesCalculatorScreen(
    modifier: Modifier = Modifier,
    chartRepo: ChartRepository = koinInject(),
) {
    var constant by remember { mutableIntStateOf(0) }
    var selectedSongId by remember { mutableStateOf<String?>(null) }
    var selectedRatingClass by remember { mutableStateOf<ArcaeaRatingClass?>(null) }
    val charts by produceState(initialValue = listOf(), selectedSongId) {
        selectedSongId?.let {
            value = chartRepo.findAllBySongId(it).firstOrNull() ?: emptyList()
        }
    }
    val enabledRatingClasses by remember {
        derivedStateOf { charts.map { it.ratingClass } }
    }
    val ratingDetails by remember {
        derivedStateOf { charts.associate { it.ratingClass to (it.rating to it.ratingPlus) } }
    }

    LaunchedEffect(selectedRatingClass, charts) {
        selectedRatingClass?.let { ratingClass ->
            charts.find { it.ratingClass == ratingClass }?.constant?.let { constant = it }
        }
    }

    SubScreenContainer(
        modifier = modifier,
        title = stringResource(UtilitiesSubScreen.Calculator.title),
    ) {
        LazyColumn(
            Modifier.padding(horizontal = dimensionResource(R.dimen.page_padding)),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding)),
                ) {
                    Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null)

                    ArcaeaPackAndSongQuickSearch(
                        onSelect = { selectedSongId = it.songId },
                    )
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding)),
                ) {
                    Icon(painterResource(R.drawable.ic_rating_class), contentDescription = null)

                    ArcaeaRatingClassSelector(
                        selectedRatingClass = selectedRatingClass,
                        onRatingClassChange = { selectedRatingClass = it },
                        enabledRatingClasses = enabledRatingClasses,
                        ratingDetails = ratingDetails,
                    )
                }
            }

            item {
                PlayRatingCalculator(
                    constant = constant,
                    isConstantReadonly = false,
                )
            }
        }
    }
}
