package xyz.sevive.arcaeaoffline.ui.database

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.common.scoreeditor.ScoreEditor
import xyz.sevive.arcaeaoffline.ui.common.scoreeditor.ScoreEditorViewModel
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard
import xyz.sevive.arcaeaoffline.ui.components.RatingClassSelector
import xyz.sevive.arcaeaoffline.ui.components.SongIdSelector

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

    Column(modifier) {
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

@Composable
fun DatabaseAddScoreScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    databaseAddScoreViewModel: DatabaseAddScoreViewModel = viewModel(factory = AppViewModelProvider.Factory),
    scoreEditorViewModel: ScoreEditorViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableIntStateOf(0) }

    val selectedChart by databaseAddScoreViewModel.chart.collectAsState()
    val score by scoreEditorViewModel.arcaeaScoreFlow.collectAsState(initial = null)

    val tabs = listOf(R.string.arcaea_chart, R.string.arcaea_score)

    SubScreenContainer(
        onNavigateUp = onNavigateUp, title = stringResource(R.string.database_add_score_title)
    ) {
        Scaffold(
            modifier,
            topBar = {
                Column {
                    Box(Modifier.defaultMinSize(minHeight = 20.dp)) {
                        AnimatedContent(
                            targetState = score != null,
                            transitionSpec = {
                                slideInHorizontally { it }.togetherWith(slideOutHorizontally { -it })
                            },
                            label = "scoreStatusCard",
                        ) {
                            when (it) {
                                true -> Row(verticalAlignment = Alignment.Bottom) {
                                    ArcaeaScoreCard(
                                        score!!,
                                        modifier = Modifier.weight(1f),
                                        chart = selectedChart,
                                    )
                                    Column {
                                        IconButton(onClick = {
                                            coroutineScope.launch {
                                                databaseAddScoreViewModel.saveScore(score!!)
                                            }
                                        }) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = stringResource(R.string.general_confirm),
                                            )
                                        }
                                    }
                                }

                                false -> Card(Modifier.fillMaxWidth()) {
                                    Text(
                                        "Score Invalid",
                                        Modifier.padding(dimensionResource(R.dimen.general_card_padding)),
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
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
            Box(Modifier.padding(it)) {
                when (currentTab) {
                    0 -> DatabaseAddScoreScreenChartSelector(
                        databaseAddScoreViewModel = databaseAddScoreViewModel,
                        scoreEditorViewModel = scoreEditorViewModel,
                    )

                    1 -> LazyColumn {
                        item {
                            ScoreEditor(viewModel = scoreEditorViewModel, overrideExpanded = false)
                        }
                    }
                }
            }
        }
    }
}
