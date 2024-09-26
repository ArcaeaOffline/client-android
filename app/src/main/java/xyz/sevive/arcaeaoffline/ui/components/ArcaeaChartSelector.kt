package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainerImpl


@Composable
private fun rememberArcaeaSong(songRepo: SongRepository, songId: String?): State<Song?> {
    return produceState<Song?>(initialValue = null, songId) {
        songId?.let {
            songRepo.find(songId).collect { value = it }
        }
    }
}

@Composable
private fun rememberArcaeaCharts(
    difficultyRepo: DifficultyRepository,
    chartRepo: ChartRepository,
    song: Song?,
    allowFakeChart: Boolean = false,
): State<List<Chart>> {
    return produceState(initialValue = emptyList(), song?.id, allowFakeChart) {
        song?.let {
            if (allowFakeChart) {
                difficultyRepo.findAllBySongId(song.id).collect { difficulties ->
                    value = difficulties.map { ChartFactory.fakeChart(song, it) }
                }
            } else {
                chartRepo.findAllBySongId(song).collect {
                    value = it
                }
            }
        }
    }
}

@Composable
fun ArcaeaChartSelector(
    chart: Chart?,
    onChartChange: (Chart?) -> Unit,
    allowFakeChart: Boolean = true,
) {
    val context = LocalContext.current
    val repositoryContainer = remember {
        ArcaeaOfflineDatabaseRepositoryContainerImpl(context)
    }

    var selectedSongId by rememberSaveable(chart?.songId) { mutableStateOf(chart?.songId) }
    var selectedRatingClass by rememberSaveable(chart?.ratingClass) {
        mutableStateOf(chart?.ratingClass)
    }
    val song by rememberArcaeaSong(songRepo = repositoryContainer.songRepo, songId = selectedSongId)
    val charts by rememberArcaeaCharts(
        difficultyRepo = repositoryContainer.difficultyRepo,
        chartRepo = repositoryContainer.chartRepo,
        song = song,
        allowFakeChart = allowFakeChart,
    )

    val ratingDetails = remember(charts) {
        charts.associate { it.ratingClass to (it.rating to it.ratingPlus) }
    }
    val enabledRatingClasses = remember(charts) {
        charts.map { it.ratingClass }
    }

    LaunchedEffect(selectedRatingClass) {
        // onChartChange handler
        selectedRatingClass?.let {
            charts.find { it.ratingClass == selectedRatingClass }?.let(onChartChange)
        }
    }

    Column(
        Modifier.padding(dimensionResource(R.dimen.card_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
    ) {
        ArcaeaPackAndSongSelector(
            song = song,
            onSongChanged = { selectedSongId = it?.id },
            chartOnly = !allowFakeChart,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_text_padding))
        ) {
            Icon(painterResource(R.drawable.ic_rating_class), contentDescription = null)

            RatingClassSelector(
                selectedRatingClass = selectedRatingClass,
                onRatingClassChange = { selectedRatingClass = it },
                enabledRatingClasses = enabledRatingClasses,
                ratingDetails = ratingDetails,
            )
        }
    }
}
