package xyz.sevive.arcaeaoffline.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val ArcaeaPastSvgMain = Color(0xFF5CBAD3)
val ArcaeaPastSvgBg = Color(0xFF328AA0)

val ArcaeaPresentSvgMain = Color(0xFFB5C76F)
val ArcaeaPresentSvgBg = Color(0xFF8C9B51)

val ArcaeaFutureSvgMain = Color(0xFF913A79)
val ArcaeaFutureSvgBg = Color(0xFF772F63)

val ArcaeaBeyondSvgMain = Color(0xFFBF0D25)
val ArcaeaBeyondSvgBg = Color(0xFFA0303F)

/**
 * Default values are light colors.
 */
@Immutable
data class ArcaeaColors(
    val past: Color = Color(0xFF5CBAD3),
    val present: Color = Color(0xFF829438),
    val future: Color = Color(0xFF913A79),
    val beyond: Color = Color(0xFFBF0D25),
    val eternal: Color = Color(0xFF8B77A4),
    val pure: Color = Color(0xFFF22EC6),
    val far: Color = Color(0xFFFF9028),
    val lost: Color = Color(0xFFFF0C43),
) {
    companion object {
        val Light = ArcaeaColors()

        val Dark = ArcaeaColors(
            past = Color(0xFF5CBAD3),
            present = Color(0xFFB5C76F),
            future = Color(0xFFC56DAC),
            beyond = Color(0xFFF24058),
            eternal = Color(0xFFD3B5F9),
        )
    }
}

val LocalArcaeaColors = staticCompositionLocalOf { ArcaeaColors() }

@Immutable
data class ArcaeaGradeGradientColors(
    val exPlus: List<Color> = listOf(Color.Unspecified, Color.Unspecified),
    val ex: List<Color> = listOf(Color.Unspecified, Color.Unspecified),
    val aa: List<Color> = listOf(Color.Unspecified, Color.Unspecified),
    val a: List<Color> = listOf(Color.Unspecified, Color.Unspecified),
    val b: List<Color> = listOf(Color.Unspecified, Color.Unspecified),
    val c: List<Color> = listOf(Color.Unspecified, Color.Unspecified),
    val d: List<Color> = listOf(Color.Unspecified, Color.Unspecified),
) {
    companion object {
        val Light = ArcaeaGradeGradientColors(
            exPlus = listOf(Color(0xff83238c), Color(0xff2c72ae)),
            ex = listOf(Color(0xff721b6b), Color(0xff295b8d)),
            aa = listOf(Color(0xff5a3463), Color(0xff9b4b8d)),
            a = listOf(Color(0xff46324d), Color(0xff92588a)),
            b = listOf(Color(0xff43334a), Color(0xff755b7c)),
            c = listOf(Color(0xff3b2b27), Color(0xff80566b)),
            d = listOf(Color(0xff5d1d35), Color(0xff9f3c55)),
        )

        val Dark = ArcaeaGradeGradientColors(
            exPlus = listOf(Color(0xffbf33cc), Color(0xff4791d1)),
            ex = listOf(Color(0xffba2cae), Color(0xff397fc6)),
            aa = listOf(Color(0xff785880), Color(0xffb464a6)),
            a = listOf(Color(0xff62476c), Color(0xffab73a3)),
            b = listOf(Color(0xff604969), Color(0xff8f7297)),
            c = listOf(Color(0xff5c433d), Color(0xff9d6c85)),
            d = listOf(Color(0xff842a4b), Color(0xffbd516c)),
        )
    }
}

val LocalArcaeaGradeGradientColors = staticCompositionLocalOf { ArcaeaGradeGradientColors() }
