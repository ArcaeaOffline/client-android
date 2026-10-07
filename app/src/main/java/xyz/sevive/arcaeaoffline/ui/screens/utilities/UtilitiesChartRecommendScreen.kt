package xyz.sevive.arcaeaoffline.ui.screens.utilities

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.calculators.calculateClearBonus
import xyz.sevive.arcaeaoffline.core.calculators.calculateInvertScoreRange
import xyz.sevive.arcaeaoffline.core.calculators.calculatePlayRating
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSongAndInfo
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaChartCard
import xyz.sevive.arcaeaoffline.ui.components.BasicAlertDialogSurface
import xyz.sevive.arcaeaoffline.ui.components.DecimalStepperTextField
import xyz.sevive.arcaeaoffline.ui.components.ListGroupHeader
import xyz.sevive.arcaeaoffline.ui.components.PlayRatingCalculator
import xyz.sevive.arcaeaoffline.ui.components.SwitchItem
import xyz.sevive.arcaeaoffline.ui.components.arcaea.OutlinedArcaeaScoreTextField
import xyz.sevive.arcaeaoffline.ui.components.arcaea.rememberArcaeaScoreTextFieldState
import xyz.sevive.arcaeaoffline.ui.components.rememberDecimalStepperTextFieldState
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.navigation.UtilitiesSubScreen
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen
import xyz.sevive.arcaeaoffline.ui.theme.spacing
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
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
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
    constructor(item: DifficultyWithSongAndInfo, initialScore: Int) : this(
        songTitle = item.difficultyWithSong.title,
        songArtist = item.difficultyWithSong.artist,
        constant = item.constant,
        initialScore = initialScore,
    )
}

@Composable
private fun PlayRatingCalculatorDialog(
    onDismissRequest: () -> Unit,
    state: PlayRatingCalculatorDialogState,
    countClearBonus: Boolean,
    clearType: ArcaeaPlayResultClearType? = null,
) {
    BasicAlertDialogSurface(onDismissRequest) { contentPadding ->
        Column(
            Modifier.padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Icon(
                Icons.Default.Calculate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs),
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
                countClearBonus = countClearBonus,
                initialClearType = clearType,
            )
        }
    }
}

data class ResultsListItemState(
    val item: DifficultyWithSongAndInfo,
    val scoreRange: IntRange,
    val targetPlayRating: Double,
    val clearType: ArcaeaPlayResultClearType? = null,
) {
    val targetScoreRange by lazy {
        calculateInvertScoreRange(
            targetPlayRating = targetPlayRating,
            constant = item.constant,
            tolerance = 1e-6,
            clearBonus = calculateClearBonus(clearType),
        )
    }

    val score by lazy {
        targetScoreRange?.let { if (it.first == 10_000_000) it.first else it.average() } ?: scoreRange.average()
    }

    val scoreText by lazy {
        ArcaeaFormatters.score(score)
    }

    val actualPlayRating by lazy {
        calculatePlayRating(score, item.constant, clearType)
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
            state.item.difficultyWithSong,
            shape = ShapeDefaults.Medium.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
            chartInfo =
                ChartInfo(
                    state.item.difficultyWithSong.songId,
                    state.item.difficultyWithSong.ratingClass,
                    state.item.constant,
                    state.item.notes,
                ),
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(state.scoreText, Modifier.padding(start = MaterialTheme.spacing.lg))

            Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null)

            Text(ArcaeaFormatters.potentialToText(state.actualPlayRating), fontWeight = FontWeight.Bold)

            Spacer(Modifier.weight(1f))

            IconButton(onClick = onOpenCalculator) {
                Icon(Icons.Default.Calculate, contentDescription = stringResource(R.string.utilities_calculator_title))
            }
        }
    }
}

private val ParametersCardCollapsedHeight = 48.dp
private val ParametersCardTopPadding = MaterialTheme.spacing.xs
private val ParametersCardOuterVerticalPadding = MaterialTheme.spacing.sm

/**
 * Floating parameters panel. It overlays the results list, which clears its
 * collapsed height via [ParametersCardCollapsedHeight].
 */
@Composable
private fun ParametersCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    summary: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val arrowRotation by animateFloatAsState(if (expanded) 0f else -90f)

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        // M3 elevation level 3 (dialog tier)
        shadowElevation = 6.dp,
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = ParametersCardCollapsedHeight)
                    .clickable { onExpandedChange(!expanded) }
                    .padding(horizontal = MaterialTheme.spacing.lg, vertical = ParametersCardTopPadding),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.utilities_recommend_input),
                    style = MaterialTheme.typography.titleMedium,
                )

                AnimatedVisibility(!expanded) {
                    summary()
                }

                Spacer(Modifier.weight(1f))

                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    Modifier.graphicsLayer { rotationZ = arrowRotation },
                )
            }

            AnimatedVisibility(expanded) {
                Column(
                    Modifier.padding(top = MaterialTheme.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    content = content,
                )
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
    var showCalculatorDialog by rememberSaveable { mutableStateOf(false) }
    var calculatorDialogState by rememberSerializable { mutableStateOf(PlayRatingCalculatorDialogState()) }

    if (showCalculatorDialog) {
        PlayRatingCalculatorDialog(
            onDismissRequest = { showCalculatorDialog = false },
            state = calculatorDialogState,
            countClearBonus = uiState.scoringMode == ArcaeaScoringMode.B50,
            clearType = uiState.clearType,
        )
    }

    SubScreenContainer(
        modifier = modifier,
        title = stringResource(UtilitiesSubScreen.Recommend.title),
    ) {
        Box(Modifier.fillMaxSize()) {
            // The card floats with top padding. The list and the top
            // scrim both clear the card's bottom edge plus a lg gap.
            val listTopInset =
                ParametersCardCollapsedHeight +
                    ParametersCardOuterVerticalPadding +
                    MaterialTheme.spacing.lg

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        top = listTopInset,
                        bottom = MaterialTheme.spacing.sm,
                    ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ListGroupHeader(stringResource(R.string.utilities_recommend_results))

                        Spacer(Modifier.weight(1f))

                        AnimatedVisibility(
                            visible = uiState.isLoading,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            CircularProgressIndicator(
                                Modifier
                                    .padding(end = MaterialTheme.spacing.lg)
                                    .size(18.dp),
                            )
                        }
                    }
                }

                if (uiState.charts.isEmpty()) {
                    item {
                        EmptyScreen(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = MaterialTheme.spacing.pagePadding),
                        )
                    }
                } else {
                    items(uiState.charts, { it.difficultyWithSong.songId + it.difficultyWithSong.ratingClass.name }) { item ->
                        val state = ResultsListItemState(item, scoreRange, targetPlayRating, uiState.clearType)

                        ResultsListItem(
                            state = state,
                            onOpenCalculator = {
                                calculatorDialogState = PlayRatingCalculatorDialogState(item, state.score)
                                showCalculatorDialog = true
                            },
                            Modifier
                                .padding(horizontal = MaterialTheme.spacing.pagePadding)
                                .animateItem(),
                        )
                    }
                }
            }

            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(listTopInset)
                    .background(
                        Brush.verticalGradient(
                            0f to MaterialTheme.colorScheme.background,
                            1f to Color.Transparent,
                        ),
                    ),
            )

            ParametersCard(
                expanded = isInputVisible,
                onExpandedChange = { isInputVisible = it },
                summary = {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.iconTextGap),
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
                },
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(
                            horizontal = MaterialTheme.spacing.pagePadding,
                            vertical = ParametersCardOuterVerticalPadding,
                        ).fillMaxWidth(),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
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

                Row(
                    Modifier.padding(horizontal = MaterialTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.iconTextGap),
                ) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal),
                    ) {
                        TextButton({ viewModel.setScoreRange(9_900_000..10_000_000) }) { Text("EX+") }
                        TextButton({ viewModel.setScoreRange(9_800_000..9_899_999) }) { Text("EX") }
                        TextButton({ viewModel.setScoreRange(9_500_000..9_799_999) }) { Text("AA") }
                    }
                }

                if (uiState.scoringMode == ArcaeaScoringMode.B50) {
                    SwitchItem(
                        value = uiState.clearType != null && uiState.clearType != ArcaeaPlayResultClearType.TRACK_LOST,
                        onValueChange = { cleared ->
                            viewModel.setClearType(if (cleared) ArcaeaPlayResultClearType.NORMAL_CLEAR else null)
                        },
                        title = stringResource(R.string.play_rating_cleared),
                        description = stringResource(R.string.play_rating_cleared_description),
                    )
                }
            }
        }
    }
}
