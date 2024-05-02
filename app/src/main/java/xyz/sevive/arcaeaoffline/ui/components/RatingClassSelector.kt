package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.graphics.ColorUtils
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.ratingClassColor
import kotlin.math.min

class RatingClassShape(private val isFirst: Boolean = false) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val width = size.width
        val height = size.height

        val ratingClassPath = Path().apply {
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

internal fun ratingClassConstantAnnotatedString(constant: Int?): AnnotatedString {
    if (constant == null) return buildAnnotatedString { append("?") }

    return buildAnnotatedString {
        append((constant / 10).toString())

        if ((78..79).contains(constant) || (89..89).contains(constant) || (97..99).contains(constant) || (107..109).contains(
                constant
            )
        ) {
            withStyle(SpanStyle(fontSize = 0.7.em)) {
                append('+')
            }
        }
    }
}

internal data class RatingClassBoxColors(val baseColor: Color) {
//    val bgColor = baseColor.copy(alpha = 0.3f)
//    val textColor = baseColor.copy()

    val selectedBgColor = baseColor.copy()
    val selectedTextColor: Color
        get() {
            val hsl = floatArrayOf(0f, 0f, 0f)
            ColorUtils.colorToHSL(baseColor.toArgb(), hsl)

            hsl[1] = min(1f, hsl[1] + 0.1f)
            hsl[2] = 0.95f
            return Color(ColorUtils.HSLToColor(hsl))
        }

    val notSelectedBgColor: Color
        get() {
            val hsl = floatArrayOf(0f, 0f, 0f)
            ColorUtils.colorToHSL(baseColor.toArgb(), hsl)

            hsl[1] = 0.015f
            hsl[2] = 0.6f
            return Color(ColorUtils.HSLToColor(hsl))
        }
    val notSelectedTextColor: Color
        get() {
            val hsl = floatArrayOf(0f, 0f, 0f)
            ColorUtils.colorToHSL(baseColor.toArgb(), hsl)

            hsl[1] = 0f
            hsl[2] = 0.28f
            return Color(ColorUtils.HSLToColor(hsl))
        }

    val disabledBgColor = Color(0xff202020)
    val disabledTextColor = Color(0xff9f9f9f)
}

@Composable
internal fun RatingClassBox(
    onClick: () -> Unit,
    baseColor: Color,
    ratingClass: ArcaeaScoreRatingClass,
    selected: Boolean = false,
    disabled: Boolean = false,
    isFirst: Boolean = false,
    constant: Int? = null,
) {
    val height = 50.dp
    val width = height * 1.4f

    val colors = RatingClassBoxColors(baseColor)

    val bgColor = if (disabled) colors.disabledBgColor
    else if (selected) colors.selectedBgColor
    else colors.notSelectedBgColor

    val textColor = if (disabled) colors.disabledTextColor
    else if (selected) colors.selectedTextColor
    else colors.notSelectedTextColor

    val constantText = ratingClassConstantAnnotatedString(constant)
    val label = ratingClass.toString()

//    val constantMaxHeight = height * 0.9f
    val labelMaxHeight = height * 0.4f

    val constantTextSize = LocalDensity.current.run { (height * 0.5f).toSp() }
    val labelTextSize = LocalDensity.current.run { (height * 0.175f).toSp() }

    Box(
        Modifier
            .size(width, height)
            .clip(RatingClassShape(isFirst))
            .background(bgColor)
            .clickable(enabled = !disabled) { onClick() }) {
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
            )

            Text(
                label,
                Modifier
                    .height(labelMaxHeight)
                    .align(Alignment.BottomCenter)
                    .offset {
                        // a (width * 0.04f).dp right offset
                        val xOffset = width.value * 0.04f * this.density

                        if (isFirst) IntOffset(0, 0)
                        else IntOffset(xOffset.toInt(), 0)
                    },
                fontSize = labelTextSize,
                textAlign = TextAlign.Center,
            )

            // TODO: a dark overlay of the constant text when selected
        }
    }
}


@Composable
fun RatingClassRowLayout(
    modifier: Modifier = Modifier, content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier, content = content
    ) { measurables, constraints ->
        // Don't constrain child views further, measure them with given constraints
        val placeables = measurables.map { measurable ->
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

@Composable
fun RatingClassSelector(
    selectedRatingClass: ArcaeaScoreRatingClass?,
    onRatingClassChange: (ArcaeaScoreRatingClass) -> Unit,
    enabledRatingClasses: List<ArcaeaScoreRatingClass> = listOf(),
    constants: Map<ArcaeaScoreRatingClass, Int?> = mapOf(),
) {
    @Composable
    fun RatingClassBoxWrapper(
        ratingClass: ArcaeaScoreRatingClass,
        isFirst: Boolean = false,
    ) {
        RatingClassBox(
            onClick = { onRatingClassChange(ratingClass) },
            baseColor = ratingClassColor(ratingClass),
            ratingClass = ratingClass,
            selected = selectedRatingClass == ratingClass,
            disabled = !enabledRatingClasses.contains(ratingClass),
            isFirst = isFirst,
            constant = constants[ratingClass],
        )
    }

    RatingClassRowLayout {
        RatingClassBoxWrapper(
            ratingClass = ArcaeaScoreRatingClass.PAST,
            isFirst = true,
        )
        RatingClassBoxWrapper(
            ratingClass = ArcaeaScoreRatingClass.PRESENT,
        )
        RatingClassBoxWrapper(
            ratingClass = ArcaeaScoreRatingClass.FUTURE,
        )

        if (enabledRatingClasses.contains(ArcaeaScoreRatingClass.BEYOND)) {
            RatingClassBoxWrapper(
                ratingClass = ArcaeaScoreRatingClass.BEYOND,
            )
        }

        if (enabledRatingClasses.contains(ArcaeaScoreRatingClass.ETERNAL)) {
            RatingClassBoxWrapper(
                ratingClass = ArcaeaScoreRatingClass.ETERNAL,
            )
        }
    }
}

@Preview
@Composable
private fun RatingClassSelectorDevicePreview() {
    var selectedRatingClass by remember { mutableStateOf(ArcaeaScoreRatingClass.PAST) }

    val ratingClasses = mutableListOf(
        ArcaeaScoreRatingClass.PAST,
        ArcaeaScoreRatingClass.PRESENT,
        ArcaeaScoreRatingClass.FUTURE,
        ArcaeaScoreRatingClass.BEYOND,
        ArcaeaScoreRatingClass.ETERNAL,
    )

    val ratingClassesCommon = ratingClasses.toMutableList()
    ratingClassesCommon.removeAll(
        arrayOf(
            ArcaeaScoreRatingClass.BEYOND, ArcaeaScoreRatingClass.ETERNAL
        )
    )

    val ratingClassesWithBeyond = ratingClasses.toMutableList()
    ratingClassesWithBeyond.remove(ArcaeaScoreRatingClass.ETERNAL)

    val ratingClassesWithEternal = ratingClasses.toMutableList()
    ratingClassesWithEternal.remove(ArcaeaScoreRatingClass.BEYOND)

    val constants = mapOf(
        ArcaeaScoreRatingClass.PAST to 30,
        ArcaeaScoreRatingClass.PRESENT to 78,
        ArcaeaScoreRatingClass.FUTURE to 107,
        ArcaeaScoreRatingClass.BEYOND to 120,
        ArcaeaScoreRatingClass.ETERNAL to 109,
    )

    ArcaeaOfflineTheme {
        Surface {
            Column {
                Text("selected $selectedRatingClass")

                Text("Common case")
                RatingClassSelector(
                    selectedRatingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                    enabledRatingClasses = ratingClassesCommon,
                )

                Text("With constant")
                RatingClassSelector(
                    selectedRatingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                    enabledRatingClasses = ratingClassesCommon,
                    constants = constants,
                )

                Text("With beyond")
                RatingClassSelector(
                    selectedRatingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                    enabledRatingClasses = ratingClassesWithBeyond,
                    constants = constants,
                )

                Text("With eternal")
                RatingClassSelector(
                    selectedRatingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                    enabledRatingClasses = ratingClassesWithEternal,
                    constants = constants,
                )

                Text("Last | ???")
                RatingClassSelector(
                    selectedRatingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                    enabledRatingClasses = listOf(ArcaeaScoreRatingClass.BEYOND),
                    constants = constants,
                )

                Text("wtf")
                RatingClassSelector(
                    selectedRatingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                    enabledRatingClasses = ratingClasses,
                    constants = constants,
                )
            }
        }
    }
}
