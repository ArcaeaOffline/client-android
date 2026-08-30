package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.graphics.ColorUtils
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.ratingClassColor
import kotlin.math.min

@Immutable
data class RatingClassSelectorItem(
    val display: ArcaeaRatingClassDisplay,
    val rating: Int? = null,
    val ratingPlus: Boolean = false,
)

fun List<Chart>.toRatingClassSelectorItems(): List<RatingClassSelectorItem> =
    map {
        RatingClassSelectorItem(
            display = ArcaeaRatingClassDisplay.of(it.ratingClass),
            rating = it.rating,
            ratingPlus = it.ratingPlus,
        )
    }

private class RatingClassShape(
    private val isFirst: Boolean = false,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val width = size.width
        val height = size.height

        val ratingClassPath =
            Path().apply {
                this.moveTo(0f, 0f)
                this.lineTo(width * 0.8f, 0f)
                this.lineTo(width, height * 0.6f)
                this.lineTo(width * 0.9f, height)

                if (isFirst) {
                    this.lineTo(0f, height)
                } else {
                    this.lineTo(width * 0.1f, height)
                    this.lineTo(width * 0.2f, height * 0.6f)
                }

                this.close()
            }

        return Outline.Generic(ratingClassPath)
    }
}

internal class RatingClassBoxColors(
    baseColor: Color,
) {
//    val bgColor = baseColor.copy(alpha = 0.3f)
//    val textColor = baseColor.copy()

    val selectedBgColor = baseColor.copy()
    val selectedTextColor: Color
    val selectedTextShadowColor: Color

    val notSelectedBgColor: Color
    val notSelectedTextColor: Color

    val disabledBgColor = Color(0xff202020)
    val disabledTextColor = Color(0xff9f9f9f)

    init {
        val selectedTextHsl = floatArrayOf(0f, 0f, 0f)
        ColorUtils.colorToHSL(baseColor.toArgb(), selectedTextHsl)

        selectedTextHsl[1] = min(1f, selectedTextHsl[1] + 0.1f)
        selectedTextHsl[2] = 0.95f
        selectedTextColor = Color(ColorUtils.HSLToColor(selectedTextHsl))

        val selectedTextShadowHsl = floatArrayOf(0f, 0f, 0f)
        ColorUtils.colorToHSL(baseColor.toArgb(), selectedTextShadowHsl)

        selectedTextShadowHsl[2] = 0.05f
        selectedTextShadowColor = Color(ColorUtils.HSLToColor(selectedTextShadowHsl))

        val notSelectedBgHsl = floatArrayOf(0f, 0f, 0f)
        ColorUtils.colorToHSL(baseColor.toArgb(), notSelectedBgHsl)

        notSelectedBgHsl[1] = 0.015f
        notSelectedBgHsl[2] = 0.6f
        notSelectedBgColor = Color(ColorUtils.HSLToColor(notSelectedBgHsl))

        val notSelectedTextHsl = floatArrayOf(0f, 0f, 0f)
        ColorUtils.colorToHSL(baseColor.toArgb(), notSelectedTextHsl)

        notSelectedTextHsl[1] = 0f
        notSelectedTextHsl[2] = 0.28f
        notSelectedTextColor = Color(ColorUtils.HSLToColor(notSelectedTextHsl))
    }
}

@Composable
internal fun RatingClassBox(
    onClick: () -> Unit,
    baseColor: Color,
    label: String,
    selected: Boolean = false,
    disabled: Boolean = false,
    isFirst: Boolean = false,
    rating: Int? = null,
    ratingPlus: Boolean = false,
) {
    val height = 50.dp
    val width = height * 1.4f

    val colors = remember(baseColor) { RatingClassBoxColors(baseColor) }

    val bgColor by animateColorAsState(
        targetValue =
            if (disabled) {
                colors.disabledBgColor
            } else if (selected) {
                colors.selectedBgColor
            } else {
                colors.notSelectedBgColor
            },
        label = "bgColor",
    )

    val textColor by animateColorAsState(
        targetValue =
            if (disabled) {
                colors.disabledTextColor
            } else if (selected) {
                colors.selectedTextColor
            } else {
                colors.notSelectedTextColor
            },
        label = "textColor",
    )

    val constantText =
        buildAnnotatedString {
            if (rating == null) {
                append("?")
            } else {
                append(rating.toString())
                if (ratingPlus) {
                    withStyle(SpanStyle(fontSize = 0.7.em)) {
                        append('+')
                    }
                }
            }
        }

//    val constantMaxHeight = height * 0.9f
    val labelMaxHeight = height * 0.4f

    val constantTextSize = LocalDensity.current.run { (height * 0.5f).toSp() }
    val labelTextSize = LocalDensity.current.run { (height * 0.175f).toSp() }

    val selectedTextShadowColor by animateColorAsState(
        targetValue = if (selected) colors.selectedTextShadowColor else bgColor,
        label = "selectedTextShadowColor",
    )
    val selectedTextShadowOffset by animateOffsetAsState(
        targetValue = if (selected) Offset(3f, 3f) else Offset(0f, 0f),
        label = "selectedTextShadowOffset",
    )
    val selectedTextShadowBlur by animateFloatAsState(
        targetValue = if (selected) 5f else 0f,
        label = "selectedTextShadowOffset",
    )

    Box(
        Modifier
            .size(width, height)
            .clip(RatingClassShape(isFirst))
            .background(bgColor)
            .clickable(enabled = !disabled) { onClick() },
    ) {
        CompositionLocalProvider(
            LocalContentColor provides textColor,
        ) {
            Text(
                constantText,
                Modifier
                    .align(Alignment.Center)
                    .offset {
                        // a (height * 0.1).dp upper offset
                        val yOffset = height.value * 0.1f * this.density
                        IntOffset(0, -yOffset.toInt())
                    },
                fontSize = constantTextSize,
                style =
                    LocalTextStyle.current.copy(
                        shadow =
                            if (selected) {
                                Shadow(
                                    color = selectedTextShadowColor,
                                    offset = selectedTextShadowOffset,
                                    blurRadius = selectedTextShadowBlur,
                                )
                            } else {
                                null
                            },
                    ),
            )

            Text(
                label,
                Modifier
                    .height(labelMaxHeight)
                    .align(Alignment.BottomCenter)
                    .offset {
                        // a (width * 0.04f).dp right offset
                        val xOffset = width.value * 0.04f * this.density

                        if (isFirst) {
                            IntOffset(0, 0)
                        } else {
                            IntOffset(xOffset.toInt(), 0)
                        }
                    },
                fontSize = labelTextSize,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RatingClassRowLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        // Don't constrain child views further, measure them with given constraints
        val placeables =
            measurables.map { measurable ->
                measurable.measure(constraints)
            }
        val layoutWidth = (placeables.sumOf { it.width } * 0.9f).toInt()

        layout(layoutWidth, placeables[0].height) {
            var xPosition = 0

            // Place children in the parent layout
            placeables.forEach { placeable ->
                placeable.placeRelative(x = xPosition, y = 0)

                xPosition += (placeable.width * (0.8f + 0.05f)).toInt()
            }
        }
    }
}

/**
 * The default order of selector items.
 */
private val SelectorSlotRatingClasses =
    listOf(
        ArcaeaRatingClass.PAST,
        ArcaeaRatingClass.PRESENT,
        ArcaeaRatingClass.FUTURE,
        ArcaeaRatingClass.BEYOND,
        ArcaeaRatingClass.ETERNAL,
    )

/**
 * Only show these items when passed in.
 */
private val SelectorOptionalSlotRatingClasses =
    setOf(ArcaeaRatingClass.BEYOND, ArcaeaRatingClass.ETERNAL)

@Composable
fun ArcaeaRatingClassSelector(
    items: List<RatingClassSelectorItem>,
    selectedRatingClass: ArcaeaRatingClass?,
    onRatingClassChange: (ArcaeaRatingClass) -> Unit,
    modifier: Modifier = Modifier,
) {
    RatingClassRowLayout(modifier = modifier) {
        SelectorSlotRatingClasses.forEach { slotRatingClass ->
            val item = items.find { it.display.ratingClass == slotRatingClass }

            if (item == null && slotRatingClass in SelectorOptionalSlotRatingClasses) {
                return@forEach
            }

            RatingClassBox(
                onClick = { onRatingClassChange(slotRatingClass) },
                baseColor =
                    item?.let {
                        ratingClassColor(it.display)
                    } ?: ratingClassColor(slotRatingClass),
                label = item?.display?.name ?: slotRatingClass.name,
                selected = selectedRatingClass == slotRatingClass,
                disabled = item == null,
                isFirst = slotRatingClass == ArcaeaRatingClass.PAST,
                rating = item?.rating,
                ratingPlus = item?.ratingPlus ?: false,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun RatingClassSelectorDevicePreview() {
    var selectedRatingClass by remember { mutableStateOf(ArcaeaRatingClass.PRESENT) }

    val itemsCommon =
        listOf(
            RatingClassSelectorItem(ArcaeaRatingClassDisplay.PAST, 3),
            RatingClassSelectorItem(ArcaeaRatingClassDisplay.PRESENT, 7, ratingPlus = true),
            RatingClassSelectorItem(ArcaeaRatingClassDisplay.FUTURE, 10, ratingPlus = true),
        )

    val itemsWithBeyond =
        itemsCommon +
            RatingClassSelectorItem(ArcaeaRatingClassDisplay.BEYOND, 12)

    val itemsWithEternal =
        itemsCommon +
            RatingClassSelectorItem(ArcaeaRatingClassDisplay.ETERNAL, 10, ratingPlus = true)

    val itemsWithInscribed =
        itemsCommon +
            RatingClassSelectorItem(ArcaeaRatingClassDisplay.INSCRIBED, 11)

    val itemsAll = itemsWithBeyond + itemsWithEternal.last()

    ArcaeaOfflineTheme {
        Surface {
            Column {
                Text("selected $selectedRatingClass")

                Text("Common case")
                ArcaeaRatingClassSelector(
                    items = itemsCommon,
                    selectedRatingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                )

                Text("With beyond")
                ArcaeaRatingClassSelector(
                    items = itemsWithBeyond,
                    selectedRatingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                )

                Text("With eternal")
                ArcaeaRatingClassSelector(
                    items = itemsWithEternal,
                    selectedRatingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                )

                Text("With inscribed")
                ArcaeaRatingClassSelector(
                    items = itemsWithInscribed,
                    selectedRatingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                )

                Text("Last | ???")
                ArcaeaRatingClassSelector(
                    items =
                        listOf(
                            RatingClassSelectorItem(ArcaeaRatingClassDisplay.BEYOND, 12),
                        ),
                    selectedRatingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                )

                Text("All")
                ArcaeaRatingClassSelector(
                    items = itemsAll,
                    selectedRatingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                )
            }
        }
    }
}
