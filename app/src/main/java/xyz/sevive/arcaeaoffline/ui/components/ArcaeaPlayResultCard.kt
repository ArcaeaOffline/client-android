package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDateTime
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.arcaeaColors
import xyz.sevive.arcaeaoffline.ui.theme.extendedColorScheme
import xyz.sevive.arcaeaoffline.ui.theme.playResultGradeGradientBrush

@Composable
private fun pflAnnotatedString(label: String, number: Int?): AnnotatedString {
    return buildAnnotatedString {
        withStyle(SpanStyle(fontSize = MaterialTheme.typography.labelMedium.fontSize)) {
            append(label)
            append(' ')
        }
        append(number?.toString() ?: "-")
    }
}

@Composable
private fun PlayResultDetailsDialog(
    onDismissRequest: () -> Unit,
    playResult: PlayResult,
) {
    val context = LocalContext.current
    val items = remember(playResult) {
        with(playResult) {
            buildMap {
                put(key = "ID", value = id.toString())
                put(key = "UUID", value = uuid.toString())
                put(key = "songId.ratingClass", value = "${songId}.${ratingClass}")
                comment?.let {
                    put(
                        key = context.getString(R.string.arcaea_play_result_comment), value = it
                    )
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
        text = {
            LazyColumn {
                items.entries.forEach {
                    item {
                        TextPreferencesWidget(title = it.key, content = it.value)
                    }
                }
            }
        },
        confirmButton = { /* Nothing */ },
    )
}


@Composable
fun ArcaeaPlayResultCard(
    playResult: PlayResult,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    warnings: List<ArcaeaPlayResultValidatorWarning> = emptyList(),
    shape: Shape = CardDefaults.shape,
    colors: CardColors? = CardDefaults.cardColors(),
) {
    val context = LocalContext.current

    val cardColors = colors ?: CardDefaults.cardColors()

    val scoreText = remember(playResult.score) {
        playResult.score.toString().padStart(8, '0').reversed().chunked(3).joinToString("'")
            .reversed()
    }
    val dateText = remember(playResult.date) {
        playResult.date?.formatAsLocalizedDateTime()
            ?: context.getString(R.string.play_result_no_date)
    }
    val clearTypeAndModifierText = remember(playResult.clearType, playResult.modifier) {
        val clearTypeText = playResult.clearType?.toDisplayString()
            ?: context.getString(R.string.play_result_no_clear_type)
        val modifierText = playResult.modifier?.toDisplayString()
            ?: context.getString(R.string.play_result_no_modifier)

        "$clearTypeText · $modifierText"
    }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val levelTextTextStyle = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        brush = playResultGradeGradientBrush(playResult.score),
    )
    val exPlusWidthDp = remember {
        density.run { textMeasurer.measure("EX+", levelTextTextStyle).size.width.toDp() }
    }

    var showDetails by rememberSaveable { mutableStateOf(false) }
    if (showDetails) {
        PlayResultDetailsDialog(onDismissRequest = { showDetails = false }, playResult = playResult)
    }

    var showWarningsDialog by rememberSaveable { mutableStateOf(false) }
    if (showWarningsDialog && warnings.isNotEmpty()) {
        ArcaeaPlayResultValidatorWarningDetailsDialog(
            onDismissRequest = { showWarningsDialog = false },
            warnings = warnings,
        )
    }
    LaunchedEffect(warnings) { if (warnings.isEmpty()) showWarningsDialog = false }

    Card(
        modifier = Modifier
            .clickable(onClick != null) { onClick?.invoke() }
            .height(IntrinsicSize.Min)
            .then(modifier),
        shape = shape,
        colors = cardColors,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier
                    .width(exPlusWidthDp + dimensionResource(R.dimen.card_padding))
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(visible = warnings.isNotEmpty()) {
                    Box(
                        Modifier
                            .clickable { showWarningsDialog = true }
                            .minimumInteractiveComponentSize()
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    contentColor = MaterialTheme.extendedColorScheme.warning,
                                    containerColor = MaterialTheme.extendedColorScheme.warningContainer,
                                ) {
                                    Text(warnings.size.toString())
                                }
                            },
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = pluralStringResource(
                                    R.plurals.play_result_validator_warning_count,
                                    warnings.size,
                                    warnings.size,
                                ),
                            )
                        }
                    }
                }

                Box(
                    Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        ArcaeaFormatters.scoreToLevelText(playResult.score),
                        style = levelTextTextStyle,
                        maxLines = 1,
                    )
                }

                Box(
                    Modifier
                        .clickable { showDetails = true }
                        .minimumInteractiveComponentSize()
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null)
                }
            }

            Column(
                Modifier
                    .weight(1f)
                    .padding(dimensionResource(R.dimen.card_padding))
            ) {
                Text(scoreText, style = MaterialTheme.typography.titleLarge)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        pflAnnotatedString("P", playResult.pure),
                        color = MaterialTheme.arcaeaColors.pure,
                    )

                    Text(
                        pflAnnotatedString("F", playResult.far),
                        color = MaterialTheme.arcaeaColors.far,
                    )

                    Text(
                        pflAnnotatedString("L", playResult.lost),
                        color = MaterialTheme.arcaeaColors.lost,
                    )
                }

                Text(
                    pflAnnotatedString(
                        stringResource(R.string.arcaea_play_result_max_recall),
                        playResult.maxRecall
                    )
                )

                Text(
                    dateText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )

                Text(
                    clearTypeAndModifierText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )
            }
        }
    }
}


@Composable
fun ArcaeaPlayResultCard(
    playResult: PlayResult,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    warnings: List<ArcaeaPlayResultValidatorWarning> = emptyList(),
    chart: Chart?,
    colors: CardColors? = CardDefaults.cardColors(),
) {
    if (chart == null) {
        ArcaeaPlayResultCard(
            playResult = playResult,
            modifier = modifier,
            onClick = onClick,
            warnings = warnings,
            colors = colors,
        )
        return
    }

    val cornerSize = remember { 8.dp }
    val upperCardShape = remember {
        RoundedCornerShape(topStart = cornerSize, topEnd = cornerSize)
    }
    val lowerCardShape = remember {
        RoundedCornerShape(bottomStart = cornerSize, bottomEnd = cornerSize)
    }

    Column(modifier) {
        ArcaeaChartCard(chart = chart, shape = upperCardShape)
        HorizontalDivider(
            thickness = 1.dp,
            color = CardDefaults.cardColors().containerColor.copy(alpha = 0.5f),
        )
        ArcaeaPlayResultCard(
            playResult = playResult,
            onClick = onClick,
            warnings = warnings,
            shape = lowerCardShape,
            colors = colors,
        )
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
