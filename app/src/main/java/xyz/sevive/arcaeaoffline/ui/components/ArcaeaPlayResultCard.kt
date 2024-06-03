package xyz.sevive.arcaeaoffline.ui.components

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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
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
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaPflExtendedColors
import xyz.sevive.arcaeaoffline.ui.theme.playResultGradeGradientBrush

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
fun ArcaeaPlayResultCard(
    playResult: PlayResult,
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors? = CardDefaults.cardColors(),
) {
    val cardColors = colors ?: CardDefaults.cardColors()

    var showDetails by rememberSaveable { mutableStateOf(false) }
    val expandArrowRotateDegree by animateFloatAsState(
        targetValue = if (showDetails) 180f else 0f, label = "expandArrow"
    )

    val idText = remember(playResult.id) { playResult.id.toString() }
    val uuidText = remember(playResult.uuid) { playResult.uuid.toString() }
    val songIdRatingClassText = remember(playResult.songId, playResult.ratingClass) {
        "${playResult.songId}.${playResult.ratingClass}"
    }
    val scoreText = remember(playResult.score) {
        playResult.score.toString().padStart(8, '0').reversed().chunked(3).joinToString("'")
            .reversed()
    }
    val dateText = remember(playResult.date) {
        playResult.date?.let {
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(
                LocalDateTime.ofInstant(it, ZoneId.systemDefault())
            )
        }
    }
    val clearTypeText = remember(playResult.clearType) { playResult.clearType?.toDisplayString() }
    val modifierText = remember(playResult.modifier) { playResult.modifier?.toDisplayString() }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val levelTextTextStyle = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        brush = playResultGradeGradientBrush(playResult.score),
    )
    val exPlusWidthDp = remember {
        density.run { textMeasurer.measure("EX+", levelTextTextStyle).size.width.toDp() }
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
                    ArcaeaFormatters.scoreToLevelText(playResult.score),
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
                            pflAnnotatedString("P", playResult.pure),
                            color = ArcaeaPflExtendedColors.current.pure,
                        )

                        Text(
                            pflAnnotatedString("F", playResult.far),
                            color = ArcaeaPflExtendedColors.current.far,
                        )

                        Text(
                            pflAnnotatedString("L", playResult.lost),
                            color = ArcaeaPflExtendedColors.current.lost,
                        )
                    }

                    Text(
                        pflAnnotatedString(
                            stringResource(R.string.arcaea_play_result_max_recall),
                            playResult.maxRecall
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
                dateText ?: stringResource(R.string.play_result_no_date),
                style = MaterialTheme.typography.labelMedium,
            )

            AnimatedVisibility(showDetails) {
                Column(Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        Modifier.padding(vertical = 2.dp),
                        thickness = 1.dp,
                        color = LocalContentColor.current.copy(0.5f),
                    )

                    VerticalGrid(
                        columns = SimpleGridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        DetailsTextWithLabel(
                            label = "UUID",
                            text = uuidText,
                            modifier = Modifier.span(2)
                        )

                        DetailsTextWithLabel(label = "ID", text = idText)

                        DetailsTextWithLabel(label = "sI.rC", text = songIdRatingClassText)

                        DetailsTextWithLabel(
                            label = stringResource(R.string.arcaea_play_result_clear_type),
                            text = clearTypeText
                                ?: stringResource(R.string.play_result_no_clear_type),
                        )

                        DetailsTextWithLabel(
                            label = stringResource(R.string.arcaea_play_result_modifier),
                            text = modifierText ?: stringResource(R.string.play_result_no_modifier),
                        )

                        DetailsTextWithLabel(
                            label = stringResource(R.string.arcaea_play_result_comment),
                            text = playResult.comment
                                ?: stringResource(R.string.play_result_no_comment),
                            modifier = Modifier.span(2),
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ArcaeaPlayResultCard(
    playResult: PlayResult,
    modifier: Modifier = Modifier,
    chart: Chart?,
    colors: CardColors? = CardDefaults.cardColors(),
) {
    if (chart == null) {
        ArcaeaPlayResultCard(playResult = playResult, modifier = modifier, colors = colors)
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
        ArcaeaPlayResultCard(playResult = playResult, shape = lowerCardShape, colors = colors)
    }
}


@Composable
private fun previewCharts(): Array<Chart> {
    fun chart(
        ratingClass: ArcaeaRatingClass,
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
            ratingClass = ArcaeaRatingClass.PAST, rating = 2, ratingPlus = false, constant = 20
        ),
        chart(
            ratingClass = ArcaeaRatingClass.PRESENT, rating = 6, ratingPlus = false, constant = 65
        ),
        chart(
            ratingClass = ArcaeaRatingClass.FUTURE, rating = 9, ratingPlus = true, constant = 96
        ),
        chart(
            ratingClass = ArcaeaRatingClass.BEYOND, rating = 12, ratingPlus = false, constant = 120
        ),
    )
}

@Composable
private fun previewPlayResults(): Array<PlayResult> {
    fun playResult(
        id: Long, ratingClass: ArcaeaRatingClass, score: Int, pure: Int?, far: Int?, lost: Int?
    ): PlayResult {
        return PlayResult(
            id = id,
            songId = "test",
            ratingClass = ratingClass,
            score = score,
            pure = pure,
            far = far,
            lost = lost,
            date = Instant.ofEpochSecond(123456),
            maxRecall = 75,
            modifier = ArcaeaPlayResultModifier.NORMAL,
            clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR,
            comment = "Test Only",
        )
    }

    return arrayOf(
        playResult(
            id = 0,
            ratingClass = ArcaeaRatingClass.PAST,
            score = 9900000,
            pure = null,
            far = null,
            lost = null
        ),
        playResult(
            id = 1,
            ratingClass = ArcaeaRatingClass.PRESENT,
            score = 9800000,
            pure = 543,
            far = 2,
            lost = 1
        ),
        playResult(
            id = 2,
            ratingClass = ArcaeaRatingClass.FUTURE,
            score = 9700000,
            pure = 1023,
            far = 45,
            lost = 23
        ),
        playResult(
            id = 3,
            ratingClass = ArcaeaRatingClass.BEYOND,
            score = 895000,
            pure = 1234,
            far = 56,
            lost = 78
        ),
    )
}

@PreviewLightDark
@Composable
private fun PlayResultCardPreview() {
    val charts = previewCharts()
    val playResults = previewPlayResults()

    AndroidThreeTen.init(LocalContext.current)

    ArcaeaOfflineTheme {
        Column {
            repeat(4) { i ->
                ArcaeaPlayResultCard(
                    playResult = playResults[i],
                    chart = if (i >= 1) charts[i] else null,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}
