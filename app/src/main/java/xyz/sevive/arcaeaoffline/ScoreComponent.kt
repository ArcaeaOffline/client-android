package xyz.sevive.arcaeaoffline.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaDifficultyExtendedColors
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaGradeGradientExtendedColors
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaPflExtendedColors
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

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

data class ArcaeaScore(
    val score: Int,
    val pure: Int?,
    val far: Int?,
    val lost: Int?,
    val date: Date?,
    val maxRecall: Int?,
    val modifier: Int?,
    val clearType: Int?,
    val comment: String?,
)

val RatingClassTexts = arrayOf("Past", "Present", "Future", "Beyond")

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
fun scorePflText(
    string: String,
    number: Int?,
    modifier: Modifier,
    color: Color = Color.Unspecified
) {
    Row(modifier.padding(end = 8.dp)) {
        Text(
            string,
            modifier
                .alignByBaseline()
                .padding(end = 1.dp),
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
        Text(number?.toString() ?: "?", modifier.alignByBaseline(), color = color)
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ScoreCard(
    title: String,
    ratingClass: Int,
    rating: Int = 0,
    ratingPlus: Boolean = false,
    arcaeaScore: ArcaeaScore,
    modifier: Modifier = Modifier,
    dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    var ratingClassText = "${RatingClassTexts[ratingClass]} ${rating}"
    if (ratingPlus) ratingClassText += "+"

    val (score, pure, far, lost) = arcaeaScore

    Surface(
        modifier
            .padding(8.dp)
            .clickable { expanded = !expanded }) {
        Column(modifier.animateContentSize()) {
            Text(
                title,
                modifier.animateContentSize(),
                style = MaterialTheme.typography.titleLarge,
                maxLines = if (expanded) 2 else 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                ratingClassText,
                style = MaterialTheme.typography.titleMedium,
                color = ratingClassColor(ratingClass)
            )

            Row {
                Row(modifier.weight(1f)) {
                    MeasureTextWidth(
                        viewToMeasure = {
                            Text(
                                "EX+",
                                modifier.padding(end = 10.dp),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }, modifier.align(Alignment.CenterVertically)
                    ) { measuredWidth ->
                        Text(
                            scoreText(score),
                            modifier
                                .width(measuredWidth)
                                .padding(end = 8.dp)
                                .align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.headlineSmall.merge(
                                TextStyle(brush = scoreGradientBrush(score))
                            ),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            maxLines = 1,
                        )
                    }

                    Column(modifier = modifier.align(Alignment.CenterVertically)) {
                        Text(score.toString(), style = MaterialTheme.typography.titleMedium)
                        Row {
                            scorePflText(
                                "P", pure, modifier, ArcaeaPflExtendedColors.current.pure
                            )
                            scorePflText("F", far, modifier, ArcaeaPflExtendedColors.current.far)
                            scorePflText(
                                "L", lost, modifier, ArcaeaPflExtendedColors.current.lost
                            )
                            scorePflText("MR", arcaeaScore.maxRecall, modifier)
                        }
                    }
                }

                TextButton(onClick = { expanded = !expanded }) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        "",
                        modifier.rotate(if (expanded) 180f else 0f)
                    )

                }
            }

            if (arcaeaScore.date != null) {
                Text(dateFormat.format(arcaeaScore.date))
            }

            if (expanded) {
                Text(arcaeaScore.comment ?: "No comment")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ScoreCardPreview(
    modifier: Modifier = Modifier
) {
    val ratings = arrayOf(2, 6, 9, 12)
    val ratingPluses = arrayOf(false, false, true, false)
    val scores = arrayOf(
        ArcaeaScore(9900000, null, null, null, Date(283375), 75, 0, 1, "Test Only"),
        ArcaeaScore(9800000, 543, 2, 1, Date(283375), 75, 0, 1, "Test Only"),
        ArcaeaScore(9700000, 1023, 45, 23, Date(283375), 75, 0, 1, "Test Only"),
        ArcaeaScore(8950000, 1234, 56, 78, Date(283375), 75, 0, 1, "Test Only"),
    )

    ArcaeaOfflineTheme {
        Column {
            for (i in 0..3) {
                ScoreCard(
                    title = "Wow Super Cool and Looooong Song Title",
                    ratingClass = i,
                    rating = ratings[i],
                    ratingPlus = ratingPluses[i],
                    arcaeaScore = scores[i]
                )
            }
        }
    }
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ScoreCardDarkPreview(
    modifier: Modifier = Modifier
) {
    val ratings = arrayOf(2, 6, 9, 12)
    val ratingPluses = arrayOf(false, false, true, false)
    val scores = arrayOf(
        ArcaeaScore(9900000, null, null, null, Date(283375), 75, 0, 1, "Test Only"),
        ArcaeaScore(9800000, 543, 2, 1, Date(283375), 75, 0, 1, "Test Only"),
        ArcaeaScore(9700000, 1023, 45, 23, Date(283375), 75, 0, 1, "Test Only"),
        ArcaeaScore(8950000, 1234, 56, 78, Date(283375), 75, 0, 1, "Test Only"),
    )

    ArcaeaOfflineTheme {
        Column {
            for (i in 0..3) {
                ScoreCard(
                    title = "Wow Super Cool and Looooong Song Title",
                    ratingClass = i,
                    rating = ratings[i],
                    ratingPlus = ratingPluses[i],
                    arcaeaScore = scores[i]
                )
            }
        }
    }
}
