package xyz.sevive.arcaeaoffline.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreClearType
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreModifier
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaDifficultyExtendedColors
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaGradeGradientExtendedColors
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaPflExtendedColors

/**
 * https://stackoverflow.com/a/70508246/16484891
 *
 * CC BY-SA 4.0
 */
@Composable
fun MeasureTextWidth(
    viewToMeasure: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (measuredWidth: Dp) -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val measuredWidth =
            subcompose("viewToMeasure", viewToMeasure)[0].measure(Constraints()).width.toDp()

        val contentPlaceable = subcompose("content") {
            content(measuredWidth)
        }[0].measure(constraints)
        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.place(0, 0)
        }
    }
}

@Composable
fun ratingClassColor(ratingClass: Int): Color {
    return arrayOf(
        ArcaeaDifficultyExtendedColors.current.past,
        ArcaeaDifficultyExtendedColors.current.present,
        ArcaeaDifficultyExtendedColors.current.future,
        ArcaeaDifficultyExtendedColors.current.beyond,
    )[ratingClass]
}

fun scoreText(score: Int): String {
    return when {
        score >= 9900000 -> "EX+"
        score >= 9800000 -> "EX"
        score >= 9500000 -> "AA"
        score >= 9200000 -> "A"
        score >= 8900000 -> "B"
        score >= 8600000 -> "C"
        else -> "D"
    }
}

@Composable
fun scoreGradientBrush(score: Int): Brush {
    val colors = when {
        score >= 9900000 -> ArcaeaGradeGradientExtendedColors.current.exPlus
        score >= 9800000 -> ArcaeaGradeGradientExtendedColors.current.ex
        score >= 9500000 -> ArcaeaGradeGradientExtendedColors.current.aa
        score >= 9200000 -> ArcaeaGradeGradientExtendedColors.current.a
        score >= 8900000 -> ArcaeaGradeGradientExtendedColors.current.b
        score >= 8600000 -> ArcaeaGradeGradientExtendedColors.current.c
        else -> ArcaeaGradeGradientExtendedColors.current.d
    }
    return Brush.verticalGradient(colors)
}

@Composable
fun ScorePflText(
    string: String, number: Int?, modifier: Modifier = Modifier, color: Color = Color.Unspecified
) {
    Row(modifier.padding(end = 8.dp)) {
        Text(
            string,
            Modifier
                .alignByBaseline()
                .padding(end = 1.dp),
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
        Text(number?.toString() ?: "-", Modifier.alignByBaseline(), color = color)
    }
}


@Composable
fun ArcaeaScoreCard(
    score: Score,
    modifier: Modifier = Modifier,
    chart: Chart? = null,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val expandArrowRotateDegree by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "expandArrow"
    )

    val title = chart?.title ?: score.songId

    val rating = chart?.rating
    val ratingPlus = chart?.ratingPlus

    val ratingClassName = ArcaeaScoreRatingClass.fromInt(score.ratingClass).name
    val ratingClassDisplayText = if (chart != null) {
        if (ratingPlus != null && ratingPlus) {
            "$ratingClassName $rating+"
        } else {
            "$ratingClassName $rating"
        }
    } else {
        "$ratingClassName ?"
    }

    val scoreText =
        score.score.toString().padStart(8, '0').reversed().chunked(3).joinToString("'").reversed()

    Card(modifier) {
        Column(
            Modifier
                .clickable { expanded = !expanded }
                .padding(dimensionResource(R.dimen.general_card_padding))) {
            Text(
                title,
                Modifier.animateContentSize(),
                maxLines = if (expanded) 2 else 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                ratingClassDisplayText,
                color = ratingClassColor(score.ratingClass),
            )

            Row {
                Row(Modifier.weight(1f)) {
                    MeasureTextWidth(
                        viewToMeasure = {
                            Text(
                                "EX+",
                                Modifier.padding(end = 10.dp),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }, Modifier.align(Alignment.CenterVertically)
                    ) { measuredWidth ->
                        Text(
                            scoreText(score.score),
                            Modifier
                                .width(measuredWidth)
                                .padding(end = 8.dp)
                                .align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.headlineSmall.merge(
                                TextStyle(brush = scoreGradientBrush(score.score))
                            ),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            maxLines = 1,
                        )
                    }

                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        Text(scoreText, fontWeight = FontWeight.Bold)
                        Row {
                            ScorePflText(
                                "P", score.pure, color = ArcaeaPflExtendedColors.current.pure
                            )
                            ScorePflText(
                                "F", score.far, color = ArcaeaPflExtendedColors.current.far
                            )
                            ScorePflText(
                                "L", score.lost, color = ArcaeaPflExtendedColors.current.lost
                            )
                            ScorePflText("MR", score.maxRecall)
                        }
                    }
                }

                TextButton(onClick = { expanded = !expanded }) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        null,
                        Modifier.rotate(expandArrowRotateDegree),
                    )
                }
            }

            Text(
                if (score.date != null) DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .format(
                        LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(score.date.toLong()), ZoneId.systemDefault()
                        )
                    ) else stringResource(R.string.score_no_date),
                style = MaterialTheme.typography.labelMedium,
            )

            AnimatedVisibility(expanded) {
                Column {
                    val clearTypeText = if (score.clearType != null) {
                        ArcaeaScoreClearType.fromInt(score.clearType).toDisplayString()
                    } else stringResource(R.string.score_no_clear_type)

                    val modifierText = if (score.modifier != null) {
                        ArcaeaScoreModifier.fromInt(score.modifier).toDisplayString()
                    } else stringResource(R.string.score_no_modifier)

                    Text(
                        "$clearTypeText | $modifierText",
                        style = MaterialTheme.typography.labelMedium,
                    )

                    Text(
                        score.comment ?: stringResource(R.string.score_no_comment),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}


@Composable
private fun previewCharts(): Array<Chart> {
    val songIdx = 75
    val songId = "test"
    val title = "Wow Super Cool and Super Loooooooooooooooooooooooooooooong Title"
    val artist = "283375"
    val set = "test"
    val side = 0
    val audioOverride = false
    val jacketOverride = false

    return arrayOf(
        Chart(
            songIdx = songIdx,
            songId = songId,
            ratingClass = 0,
            rating = 2,
            ratingPlus = false,
            title = title,
            artist = artist,
            set = set,
            side = side,
            audioOverride = audioOverride,
            jacketOverride = jacketOverride,
            constant = 20
        ),
        Chart(
            songIdx = songIdx,
            songId = songId,
            ratingClass = 1,
            rating = 6,
            ratingPlus = false,
            title = title,
            artist = artist,
            set = set,
            side = side,
            audioOverride = audioOverride,
            jacketOverride = jacketOverride,
            constant = 65
        ),
        Chart(
            songIdx = songIdx,
            songId = songId,
            ratingClass = 2,
            rating = 9,
            ratingPlus = true,
            title = title,
            artist = artist,
            set = set,
            side = side,
            audioOverride = audioOverride,
            jacketOverride = jacketOverride,
            constant = 96
        ),
        Chart(
            songIdx = songIdx,
            songId = songId,
            ratingClass = 3,
            rating = 12,
            ratingPlus = false,
            title = title,
            artist = artist,
            set = set,
            side = side,
            audioOverride = audioOverride,
            jacketOverride = jacketOverride,
            constant = 120
        ),
    )
}

@Composable
private fun previewScores(): Array<Score> {
    return arrayOf(
        Score(0, "test", 0, 9900000, null, null, null, 283375, 75, 0, 1, "Test Only"),
        Score(0, "test", 1, 9800000, 543, 2, 1, 283375, 75, 0, 1, "Test Only"),
        Score(0, "test", 2, 9700000, 1023, 45, 23, 283375, 75, 0, 1, "Test Only"),
        Score(0, "test", 3, 895000, 1234, 56, 78, 283375, 75, 0, 1, "Test Only"),
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
                ArcaeaScoreCard(
                    chart = charts[i], score = scores[i], modifier = modifier
                )
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
                ArcaeaScoreCard(
                    chart = charts[i], score = scores[i], modifier = modifier
                )
            }
        }
    }
}
