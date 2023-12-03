package xyz.sevive.arcaeaoffline.ui.database

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard
import xyz.sevive.arcaeaoffline.ui.components.RatingClassSelector
import xyz.sevive.arcaeaoffline.ui.components.SongIdSelector
import xyz.sevive.arcaeaoffline.ui.components.scoreeditor.ScoreEditor
import xyz.sevive.arcaeaoffline.ui.components.scoreeditor.ScoreEditorViewModel

@Composable
fun DatabaseAddScoreScreenChartSelector(
    databaseAddScoreViewModel: DatabaseAddScoreViewModel,
    scoreEditorViewModel: ScoreEditorViewModel,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    val enabledRatingClasses by databaseAddScoreViewModel.enabledRatingClasses.collectAsState()

    var songId by remember { mutableStateOf("") }
    var ratingClass by remember { mutableStateOf<ArcaeaScoreRatingClass?>(null) }

    LaunchedEffect(songId, ratingClass) {
        if (songId != "" && ratingClass != null) {
            databaseAddScoreViewModel.setChart(songId, ratingClass!!.value)
            scoreEditorViewModel.setChart(songId, ratingClass!!.value)
        }
    }

    Surface(modifier) {
        Column {
            SongIdSelector({
                if (it != null) {
                    songId = it
                    coroutineScope.launch {
                        databaseAddScoreViewModel.updateEnabledRatingClasses(it)
                    }
                }
            })
            RatingClassSelector(
                ratingClass = ratingClass,
                onRatingClassChange = {
                    ratingClass = it
                },
                enabledRatingClasses = enabledRatingClasses,
            )
        }
    }
}

@Composable
fun DatabaseAddScoreScreen(
    modifier: Modifier = Modifier,
    databaseAddScoreViewModel: DatabaseAddScoreViewModel = viewModel(factory = AppViewModelProvider.Factory),
    scoreEditorViewModel: ScoreEditorViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableIntStateOf(0) }

    val selectedChart by databaseAddScoreViewModel.chart.collectAsState()
    val score by scoreEditorViewModel.arcaeaScoreFlow.collectAsState(initial = null)

    val tabs = listOf(R.string.arcaea_chart, R.string.arcaea_score)

    Surface(modifier) {
        Scaffold(
            topBar = {
                Column {
                    if (score != null) {
                        ArcaeaScoreCard(score!!, chart = selectedChart)
                    } else {
                        Text("Score Invalid")
                    }

                    TabRow(selectedTabIndex = currentTab) {
                        tabs.forEachIndexed { i, stringResId ->
                            Tab(
                                text = { Text(stringResource(stringResId)) },
                                selected = currentTab == i,
                                onClick = { currentTab = i },
                            )
                        }
                    }
                }
            },
        ) {
            Surface(Modifier.padding(it)) {
                when (currentTab) {
                    0 -> DatabaseAddScoreScreenChartSelector(
                        databaseAddScoreViewModel = databaseAddScoreViewModel,
                        scoreEditorViewModel = scoreEditorViewModel,
                    )


                    1 -> LazyColumn {
                        item {
                            ScoreEditor(
                                {
                                    coroutineScope.launch {
                                        databaseAddScoreViewModel.saveScore(it)
                                    }
                                },
                                viewModel = scoreEditorViewModel,
                            )
                        }
                    }
                }
            }
        }
    }
}
