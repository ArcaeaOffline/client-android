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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.flow.firstOrNull
import org.koin.compose.koinInject
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.core.database.entities.Song
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository

@Composable
private fun rememberArcaeaSong(
    songRepo: SongRepository,
    songId: String?,
): State<Song?> =
    produceState<Song?>(initialValue = null, songId) {
        songId?.let {
            songRepo.find(songId).collect { value = it }
        }
    }

@Composable
private fun rememberArcaeaDifficulties(
    difficultyRepo: DifficultyRepository,
    song: Song?,
): State<List<Difficulty>> =
    produceState(initialValue = emptyList(), song?.id) {
        song?.let {
            difficultyRepo.findAllBySongId(it.id).collect { value = it }
        }
    }

@Composable
private fun rememberArcaeaCharts(
    chartRepo: ChartRepository,
    song: Song?,
    difficulties: List<Difficulty>,
    allowFakeChart: Boolean = false,
): State<List<Chart>> =
    produceState(initialValue = emptyList(), song?.id, difficulties, allowFakeChart) {
        song?.let {
            value =
                difficulties.mapNotNull { difficulty ->
                    var chart = chartRepo.find(difficulty.songId, difficulty.ratingClass).firstOrNull()
                    if (chart == null && allowFakeChart) {
                        chart = ChartFactory.fakeChart(song, difficulty)
                    }
                    chart
                }
        }
    }

@Composable
fun ArcaeaChartSelector(
    chart: Chart?,
    onChartChange: (Chart?) -> Unit,
    allowFakeChart: Boolean = true,
) {
    val songRepo = koinInject<SongRepository>()
    val difficultyRepo = koinInject<DifficultyRepository>()
    val chartRepo = koinInject<ChartRepository>()

    var selectedSongId by rememberSaveable(chart?.songId) { mutableStateOf(chart?.songId) }
    var selectedRatingClass by rememberSaveable(chart?.ratingClass) {
        mutableStateOf(chart?.ratingClass)
    }
    val song by rememberArcaeaSong(songRepo = songRepo, songId = selectedSongId)
    val difficulties by rememberArcaeaDifficulties(difficultyRepo = difficultyRepo, song = song)
    val charts by rememberArcaeaCharts(
        chartRepo = chartRepo,
        song = song,
        difficulties = difficulties,
        allowFakeChart = allowFakeChart,
    )

    val selectorItems =
        remember(difficulties) {
            difficulties.toRatingClassSelectorItems()
        }

    // Emit the change when either [selectedSongId] or [selectedRatingClass] changes.
    // Since [selectedSongId] will eventually cause [charts] to change, and considering
    // the produceState delay, we use [charts] as key instead.
    LaunchedEffect(charts, selectedRatingClass) {
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
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_text_padding)),
        ) {
            Icon(painterResource(R.drawable.ic_rating_class), contentDescription = null)

            ArcaeaRatingClassSelector(
                items = selectorItems,
                selectedRatingClass = selectedRatingClass,
                onRatingClassChange = { selectedRatingClass = it },
            )
        }
    }
}
