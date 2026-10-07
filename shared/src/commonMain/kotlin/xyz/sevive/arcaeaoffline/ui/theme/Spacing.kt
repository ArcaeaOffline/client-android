package xyz.sevive.arcaeaoffline.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale on a 4dp grid, for paddings and content gaps only.
 * Icon sizes, corner radii, and stroke widths are not part of this scale.
 */
object Spacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp

    val pagePadding: Dp = lg
    val cardPadding: Dp = sm
    val iconTextGap: Dp = xs
    val dialogPadding: Dp = xl
}

/**
 * Placeholder for future CompositionLocal runtime-varying scale if introduced.
 */
val MaterialTheme.spacing: Spacing
    get() = Spacing
