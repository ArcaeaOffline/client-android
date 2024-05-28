package xyz.sevive.arcaeaoffline.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaPflExtendedColors
import xyz.sevive.arcaeaoffline.ui.theme.scoreGradientBrush

@Composable
internal fun pflAnnotatedString(label: String, number: Int?): AnnotatedString {
    return buildAnnotatedString {
        withStyle(SpanStyle(fontSize = MaterialTheme.typography.labelMedium.fontSize)) {
            append(label)
            append(' ')
        }
        append(number?.toString() ?: "-")
    }
}

@Composable
internal fun DetailsTextWithLabel(label: String, text: String, modifier: Modifier = Modifier) {
    TextWithLabel(
        text = { Text(text = text, style = MaterialTheme.typography.labelMedium) },
        label = { color, _ ->
            Text(
                text = label,
                color = color,
                style = MaterialTheme.typography.labelSmall,
            )
        },
        modifier = modifier,
    )
}

@Composable
fun ArcaeaScoreCard(
    score: Score,
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors? = CardDefaults.cardColors(),
) {
    val cardColors = colors ?: CardDefaults.cardColors()

    var showDetails by rememberSaveable { mutableStateOf(false) }
    val expandArrowRotateDegree by animateFloatAsState(
        targetValue = if (showDetails) 180f else 0f, label = "expandArrow"
    )

    val scoreText =
        score.score.toString().padStart(8, '0').reversed().chunked(3).joinToString("'").reversed()

    val textMeasurer = rememberTextMeasurer()
    val levelTextTextStyle = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        brush = scoreGradientBrush(score.score),
    )
    val exPlusWidthDp = LocalDensity.current.run {
        textMeasurer.measure("EX+", levelTextTextStyle).size.width.toDp()
    }

    Card(
        onClick = { showDetails = !showDetails },
        modifier = modifier,
        shape = shape,
        colors = cardColors,
    ) {
        Column(Modifier.padding(dimensionResource(R.dimen.card_padding))) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    ArcaeaFormatters.scoreToLevelText(score.score),
                    Modifier
                        .width(exPlusWidthDp)
                        .padding(end = 4.dp),
                    style = levelTextTextStyle,
                    textAlign = TextAlign.Right,
                    maxLines = 1,
                )

                Column(Modifier.weight(1f)) {
                    Text(scoreText, style = MaterialTheme.typography.titleLarge)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            pflAnnotatedString("P", score.pure),
                            color = ArcaeaPflExtendedColors.current.pure,
                        )

                        Text(
                            pflAnnotatedString("F", score.far),
                            color = ArcaeaPflExtendedColors.current.far,
                        )

                        Text(
                            pflAnnotatedString("L", score.lost),
                            color = ArcaeaPflExtendedColors.current.lost,
                        )
                    }

                    Text(
                        pflAnnotatedString(
                            stringResource(R.string.arcaea_max_recall), score.maxRecall
                        )
                    )
                }

                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    Modifier.graphicsLayer { rotationZ = expandArrowRotateDegree },
                )
            }

            Text(
                if (score.date != null) DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .format(
                        LocalDateTime.ofInstant(score.date, ZoneId.systemDefault())
                    ) else stringResource(R.string.score_no_date),
                style = MaterialTheme.typography.labelMedium,
            )

            AnimatedVisibility(showDetails) {
                Column(Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        Modifier.padding(vertical = 2.dp),
                        thickness = 1.dp,
                        color = LocalContentColor.current.copy(0.5f),
                    )

                    val clearTypeText = if (score.clearType != null) {
                        score.clearType!!.toDisplayString()
                    } else stringResource(R.string.score_no_clear_type)

                    val modifierText = if (score.modifier != null) {
                        score.modifier!!.toDisplayString()
                    } else stringResource(R.string.score_no_modifier)

                    VerticalGrid(
                        columns = SimpleGridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        DetailsTextWithLabel(
                            label = "ID",
                            text = if (score.id == 0) "/" else score.id.toString(),
                        )

                        DetailsTextWithLabel(
                            label = "sI.rC",
                            text = "${score.songId}.${score.ratingClass}",
                        )

                        DetailsTextWithLabel(
                            label = stringResource(R.string.arcaea_score_clear_type),
                            text = clearTypeText,
                        )

                        DetailsTextWithLabel(
                            label = stringResource(R.string.arcaea_score_modifier),
                            text = modifierText,
                        )

                        DetailsTextWithLabel(
                            label = stringResource(R.string.arcaea_score_comment),
                            text = score.comment ?: stringResource(R.string.score_no_comment),
                            modifier = Modifier.span(2),
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ArcaeaScoreCard(
    score: Score,
    modifier: Modifier = Modifier,
    chart: Chart?,
    colors: CardColors? = CardDefaults.cardColors(),
) {
    if (chart == null) {
        ArcaeaScoreCard(score = score, modifier = modifier, colors = colors)
        return
    }

    val cornerSize = 8.dp

    val upperCardShape = RoundedCornerShape(topStart = cornerSize, topEnd = cornerSize)
    val lowerCardShape = RoundedCornerShape(bottomStart = cornerSize, bottomEnd = cornerSize)

    Column(modifier) {
        ArcaeaChartCard(chart = chart, shape = upperCardShape)
        HorizontalDivider(
            thickness = 1.dp,
            color = CardDefaults.cardColors().containerColor.copy(alpha = 0.5f),
        )
        ArcaeaScoreCard(score = score, shape = lowerCardShape, colors = colors)
    }
}


@Composable
private fun previewCharts(): Array<Chart> {
    fun chart(
        ratingClass: ArcaeaScoreRatingClass,
        rating: Int,
        ratingPlus: Boolean,
        constant: Int,
    ): Chart {
        return Chart(
            songIdx = 75,
            songId = "test",
            title = "TestTitle",
            artist = "TestArtist",
            set = "test",
            side = 0,
            audioOverride = false,
            jacketOverride = false,

            ratingClass = ratingClass,
            rating = rating,
            ratingPlus = ratingPlus,
            constant = constant
        )
    }

    return arrayOf(
        chart(
            ratingClass = ArcaeaScoreRatingClass.PAST, rating = 2, ratingPlus = false, constant = 20
        ),
        chart(
            ratingClass = ArcaeaScoreRatingClass.PRESENT,
            rating = 6,
            ratingPlus = false,
            constant = 65
        ),
        chart(
            ratingClass = ArcaeaScoreRatingClass.FUTURE,
            rating = 9,
            ratingPlus = true,
            constant = 96
        ),
        chart(
            ratingClass = ArcaeaScoreRatingClass.BEYOND,
            rating = 12,
            ratingPlus = false,
            constant = 120
        ),
    )
}

@Composable
private fun previewScores(): Array<Score> {
    fun score(
        id: Int, ratingClass: ArcaeaScoreRatingClass, score: Int, pure: Int?, far: Int?, lost: Int?
    ): Score {
        return Score(
            id = id,
            songId = "test",
            ratingClass = ratingClass,
            score = score,
            pure = pure,
            far = far,
            lost = lost,
            date = Instant.ofEpochSecond(123456),
            maxRecall = 75,
            modifier = ArcaeaScoreModifier.NORMAL,
            clearType = ArcaeaScoreClearType.NORMAL_CLEAR,
            comment = "Test Only",
        )
    }

    return arrayOf(
        score(
            id = 0,
            ratingClass = ArcaeaScoreRatingClass.PAST,
            score = 9900000,
            pure = null,
            far = null,
            lost = null
        ),
        score(
            id = 1,
            ratingClass = ArcaeaScoreRatingClass.PRESENT,
            score = 9800000,
            pure = 543,
            far = 2,
            lost = 1
        ),
        score(
            id = 2,
            ratingClass = ArcaeaScoreRatingClass.FUTURE,
            score = 9700000,
            pure = 1023,
            far = 45,
            lost = 23
        ),
        score(
            id = 3,
            ratingClass = ArcaeaScoreRatingClass.BEYOND,
            score = 895000,
            pure = 1234,
            far = 56,
            lost = 78
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ScoreCardPreview(
    modifier: Modifier = Modifier
) {
    val charts = previewCharts()
    val scores = previewScores()

    AndroidThreeTen.init(LocalContext.current)

    ArcaeaOfflineTheme {
        Column {
            for (i in 0..3) {
                if (i >= 1) {
                    ArcaeaScoreCard(score = scores[i], chart = charts[i], modifier = modifier)
                } else {
                    ArcaeaScoreCard(score = scores[i], modifier = modifier)
                }
            }
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ScoreCardDarkPreview(
    modifier: Modifier = Modifier
) {
    val charts = previewCharts()
    val scores = previewScores()

    AndroidThreeTen.init(LocalContext.current)

    ArcaeaOfflineTheme {
        Column {
            for (i in 0..3) {
                if (i >= 1) {
                    ArcaeaScoreCard(score = scores[i], chart = charts[i], modifier = modifier)
                } else {
                    ArcaeaScoreCard(score = scores[i], modifier = modifier)
                }
            }
        }
    }
}
