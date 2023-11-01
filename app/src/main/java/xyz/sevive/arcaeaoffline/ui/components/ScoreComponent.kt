package xyz.sevive.arcaeaoffline.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
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
import xyz.sevive.arcaeaoffline.database.entities.Chart
import xyz.sevive.arcaeaoffline.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.database.entities.Score
import xyz.sevive.arcaeaoffline.database.entities.Song
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaDifficultyExtendedColors
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaGradeGradientExtendedColors
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaPflExtendedColors
import java.text.DateFormat
import java.text.SimpleDateFormat

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
    score: Score,
    chart: Chart? = null,
    song: Song? = null,
    difficulty: Difficulty? = null,
    modifier: Modifier = Modifier,
    dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    var title = score.songId
    if (chart != null) {
        title = chart.title
    } else if (difficulty != null && difficulty.title != null) {
        title = difficulty.title
    } else if (song != null) {
        title = song.title
    }

    var rating: Int? = null
    var ratingPlus = false
    if (chart != null) {
        rating = chart.rating
        ratingPlus = chart.ratingPlus
    } else if (difficulty != null) {
        rating = difficulty.rating
        ratingPlus = difficulty.ratingPlus
    }

    var ratingClassText = "${RatingClassTexts[score.ratingClass]} $rating"
    if (ratingPlus) ratingClassText += "+"

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
                color = ratingClassColor(score.ratingClass)
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
                            scoreText(score.score),
                            modifier
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

                    Column(modifier = modifier.align(Alignment.CenterVertically)) {
                        Text(score.score.toString(), style = MaterialTheme.typography.titleMedium)
                        Row {
                            scorePflText(
                                "P", score.pure, modifier, ArcaeaPflExtendedColors.current.pure
                            )
                            scorePflText(
                                "F",
                                score.far,
                                modifier,
                                ArcaeaPflExtendedColors.current.far
                            )
                            scorePflText(
                                "L", score.lost, modifier, ArcaeaPflExtendedColors.current.lost
                            )
                            scorePflText("MR", score.maxRecall, modifier)
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

            if (score.date != null) {
                Text(dateFormat.format(score.date))
            }

            AnimatedVisibility(expanded) {
                Text(score.comment ?: "No comment")
            }
        }
    }
}


@Composable
fun GetPreviewCharts(): Array<Chart> {
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
fun GetPreviewScores(): Array<Score> {
    return arrayOf(
        Score(0, "test", 0, 9900000, null, null, null, 283375, 75, 0, 1, "Test Only"),
        Score(0, "test", 1, 9800000, 543, 2, 1, 283375, 75, 0, 1, "Test Only"),
        Score(0, "test", 2, 9700000, 1023, 45, 23, 283375, 75, 0, 1, "Test Only"),
        Score(0, "test", 3, 8950000, 1234, 56, 78, 283375, 75, 0, 1, "Test Only"),
    )
}

@Preview(showBackground = true)
@Composable
fun ScoreCardPreview(
    modifier: Modifier = Modifier
) {
    val charts = GetPreviewCharts()
    val scores = GetPreviewScores()

    ArcaeaOfflineTheme {
        Column {
            for (i in 0..3) {
                ScoreCard(
                    chart = charts[i], score = scores[i], modifier = modifier
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
    val charts = GetPreviewCharts()
    val scores = GetPreviewScores()

    ArcaeaOfflineTheme {
        Column {
            for (i in 0..3) {
                ScoreCard(
                    chart = charts[i], score = scores[i], modifier = modifier
                )
            }
        }
    }
}
