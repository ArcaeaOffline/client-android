package xyz.sevive.arcaeaoffline.ui.screens.utilities

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.calculators.calculateInvertScoreRange
import xyz.sevive.arcaeaoffline.core.calculators.calculatePlayRating
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaChartCard
import xyz.sevive.arcaeaoffline.ui.components.BasicAlertDialogSurface
import xyz.sevive.arcaeaoffline.ui.components.DecimalStepperTextField
import xyz.sevive.arcaeaoffline.ui.components.ListGroupHeader
import xyz.sevive.arcaeaoffline.ui.components.arcaea.OutlinedArcaeaScoreTextField
import xyz.sevive.arcaeaoffline.ui.components.PlayRatingCalculator
import xyz.sevive.arcaeaoffline.ui.components.arcaea.rememberArcaeaScoreTextFieldState
import xyz.sevive.arcaeaoffline.ui.components.rememberDecimalStepperTextFieldState
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.navigation.UtilitiesSubScreen
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen
import kotlin.math.round

private fun IntRange.average() = round((first + last) / 2.0).toInt()

@Composable
private fun ScoreRangeInput(
    scoreRange: IntRange,
    onRangeFirstChange: (Int) -> Unit,
    onRangeLastChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rangeFirstTextFieldState = rememberArcaeaScoreTextFieldState(scoreRange.first)
    val rangeLastTextFieldState = rememberArcaeaScoreTextFieldState(scoreRange.last)

    LaunchedEffect(rangeFirstTextFieldState.intValue, rangeLastTextFieldState.intValue) {
        rangeFirstTextFieldState.intValue?.let { onRangeFirstChange(it) }
    }

    LaunchedEffect(rangeLastTextFieldState.intValue) {
        rangeLastTextFieldState.intValue?.let { onRangeLastChange(it) }
    }

    LaunchedEffect(scoreRange) {
        if (rangeFirstTextFieldState.intValue != scoreRange.first) {
            rangeFirstTextFieldState.updateValue(scoreRange.first)
        }
        if (rangeLastTextFieldState.intValue != scoreRange.last) {
            rangeLastTextFieldState.updateValue(scoreRange.last)
        }
    }

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedArcaeaScoreTextField(
            rangeLastTextFieldState,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.utilities_recommend_maximum_score)) },
        )

        OutlinedArcaeaScoreTextField(
            rangeFirstTextFieldState,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.utilities_recommend_minimum_score)) },
        )
    }
}

@Serializable
data class PlayRatingCalculatorDialogState(
    val songTitle: String = "",
    val songArtist: String = "",
    val constant: Int = 0,
    val initialScore: Int = 0,
) {
    constructor(chart: Chart, initialScore: Int) : this(
        songTitle = chart.title,
        songArtist = chart.artist,
        constant = chart.constant,
        initialScore = initialScore,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayRatingCalculatorDialog(
    onDismissRequest: () -> Unit,
    state: PlayRatingCalculatorDialogState,
) {
    BasicAlertDialogSurface(onDismissRequest) { contentPadding ->
        Column(
            Modifier.padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Default.Calculate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(state.songTitle, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal))
                Text(
                    state.songArtist,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            PlayRatingCalculator(
                score = state.initialScore,
                constant = state.constant,
                isConstantReadonly = true,
                initialFocusScoreTextField = true,
            )
        }
    }
}

data class ResultsListItemState(
    val chart: Chart,
    val scoreRange: IntRange,
    val targetPlayRating: Double,
) {
    val targetScoreRange by lazy {
        calculateInvertScoreRange(
            targetPlayRating = targetPlayRating,
            constant = chart.constant,
            tolerance = 1e-6,
        )
    }

    val score by lazy {
        targetScoreRange?.let { if (it.first == 10_000_000) it.first else it.average() } ?: scoreRange.average()
    }

    val scoreText by lazy {
        ArcaeaFormatters.score(score)
    }

    val actualPlayRating by lazy {
        calculatePlayRating(score, chart.constant)
    }
}

@Composable
private fun ResultsListItem(
    state: ResultsListItemState,
    onOpenCalculator: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier) {
        ArcaeaChartCard(
            chart = state.chart,
            shape = ShapeDefaults.Medium.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(state.scoreText, Modifier.padding(start = 16.dp))

            Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null)

            Text("%.2f".format(state.actualPlayRating), fontWeight = FontWeight.Bold)

            Spacer(Modifier.weight(1f))

            IconButton(onClick = onOpenCalculator) {
                Icon(Icons.Default.Calculate, contentDescription = stringResource(R.string.utilities_calculator_title))
            }
        }
    }
}

@Composable
fun UtilitiesChartRecommendScreen(
    modifier: Modifier = Modifier,
    viewModel: UtilitiesChartRecommendScreenViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scoreRange = uiState.scoreRange
    val targetPlayRating = uiState.targetPlayRating

    val targetPlayRatingTextFieldState =
        rememberDecimalStepperTextFieldState(
            initialValue = uiState.targetPlayRating,
            maxDecimalPlaces = 2,
            minValue = 0.0,
            step = 0.1,
        )

    LaunchedEffect(targetPlayRatingTextFieldState.value) {
        targetPlayRatingTextFieldState.doubleValue?.let {
            if (it != targetPlayRating) viewModel.setTargetPlayRating(it)
        }
    }

    LaunchedEffect(targetPlayRating) {
        if (targetPlayRatingTextFieldState.doubleValue != targetPlayRating) {
            targetPlayRatingTextFieldState.commitValue(targetPlayRating)
        }
    }

    var isInputVisible by rememberSaveable { mutableStateOf(false) }
    val expandArrowRotateDegree by animateFloatAsState(
        if (isInputVisible) 0f else -90f,
    )
    var showCalculatorDialog by rememberSaveable { mutableStateOf(false) }
    var calculatorDialogState by rememberSerializable { mutableStateOf(PlayRatingCalculatorDialogState()) }

    if (showCalculatorDialog) {
        PlayRatingCalculatorDialog(
            onDismissRequest = { showCalculatorDialog = false },
            state = calculatorDialogState,
        )
    }

    SubScreenContainer(
        modifier = modifier,
        title = stringResource(UtilitiesSubScreen.Recommend.title),
    ) {
        Column(Modifier.padding(horizontal = dimensionResource(R.dimen.page_padding))) {
            Row(
                Modifier.clickable { isInputVisible = !isInputVisible },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ListGroupHeader(stringResource(R.string.utilities_recommend_input))

                AnimatedVisibility(!isInputVisible) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_text_padding)),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("${ArcaeaFormatters.score(scoreRange.first)} ~ ${ArcaeaFormatters.score(scoreRange.last)}")

                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                Modifier
                                    .size(16.dp)
                                    .rotate(-45f),
                            )

                            Text(targetPlayRating.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    Modifier.graphicsLayer { rotationZ = expandArrowRotateDegree },
                )
            }

            AnimatedVisibility(isInputVisible) {
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ScoreRangeInput(
                            scoreRange = scoreRange,
                            onRangeFirstChange = { viewModel.setScoreRange(it..scoreRange.last) },
                            onRangeLastChange = { viewModel.setScoreRange(scoreRange.first..it) },
                            Modifier.weight(1f),
                        )

                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            Modifier.rotate(-45f),
                        )

                        DecimalStepperTextField(
                            targetPlayRatingTextFieldState,
                            Modifier.weight(1f),
                            label = { Text(stringResource(R.string.utilities_recommend_target_play_rating)) },
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_text_padding))) {
                        CompositionLocalProvider(
                            LocalTextStyle provides MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                        ) {
                            TextButton({ viewModel.setScoreRange(9_900_000..10_000_000) }) { Text("EX+") }
                            TextButton({ viewModel.setScoreRange(9_800_000..9_899_999) }) { Text("EX") }
                            TextButton({ viewModel.setScoreRange(9_500_000..9_799_999) }) { Text("AA") }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                ListGroupHeader(stringResource(R.string.utilities_recommend_results))

                Spacer(Modifier.weight(1f))

                AnimatedVisibility(
                    visible = uiState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    CircularProgressIndicator(Modifier.size(18.dp))
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.list_padding)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            ) {
                if (uiState.charts.isEmpty()) {
                    item {
                        EmptyScreen(Modifier.fillMaxSize())
                    }
                } else {
                    items(uiState.charts, { it.songId + it.ratingClass.name }) { chart ->
                        val state = ResultsListItemState(chart, scoreRange, targetPlayRating)

                        ResultsListItem(
                            state = state,
                            onOpenCalculator = {
                                calculatorDialogState = PlayRatingCalculatorDialogState(state.chart, state.score)
                                showCalculatorDialog = true
                            },
                            Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}
