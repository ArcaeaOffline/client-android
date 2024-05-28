package xyz.sevive.arcaeaoffline.ui.common.customtab

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import xyz.sevive.arcaeaoffline.R

enum class CustomTabIndicatorPosition {
    BOTTOM, END
}

@Composable
private fun CustomTabContent(
    text: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.icon_text_padding)),
    ) {
        if (icon != null) {
            icon()
        }

        Text(text)
    }
}

@Composable
fun CustomTabIndicator(
    selected: Boolean,
    maxWidth: Int,
    maxHeight: Int,
    indicatorPosition: CustomTabIndicatorPosition,
    indicatorLength: Dp,
    indicatorThickness: Dp,
) {
    val maxWidthDp = LocalDensity.current.run { maxWidth.toDp() }
    val maxHeightDp = LocalDensity.current.run { maxHeight.toDp() }

    val indicatorBoxWidth = when (indicatorPosition) {
        CustomTabIndicatorPosition.BOTTOM -> indicatorLength
        CustomTabIndicatorPosition.END -> indicatorThickness
    }
    val indicatorBoxHeight = when (indicatorPosition) {
        CustomTabIndicatorPosition.BOTTOM -> indicatorThickness
        CustomTabIndicatorPosition.END -> indicatorLength
    }
    val xOffset = when (indicatorPosition) {
        CustomTabIndicatorPosition.BOTTOM -> (maxWidthDp - indicatorBoxWidth) / 2f
        CustomTabIndicatorPosition.END -> maxWidthDp - indicatorBoxWidth
    }
    val yOffset = when (indicatorPosition) {
        CustomTabIndicatorPosition.BOTTOM -> maxHeightDp - indicatorBoxHeight
        CustomTabIndicatorPosition.END -> (maxHeightDp - indicatorBoxHeight) / 2f
    }

    val colorAlpha by animateFloatAsState(if (selected) 1f else 0f, label = "indicatorColorAlpha")

    Box(
        Modifier
            .offset(x = xOffset, y = yOffset)
            .width(indicatorBoxWidth)
            .height(indicatorBoxHeight)
            .background(LocalContentColor.current.copy(colorAlpha))
    )
}

@Composable
fun CustomTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    paddingValues: PaddingValues = CustomTabDefaults.paddingValues,
    indicatorPosition: CustomTabIndicatorPosition = CustomTabDefaults.indicatorPosition,
    indicatorLength: Dp = CustomTabDefaults.indicatorLength,
    indicatorThickness: Dp = CustomTabDefaults.indicatorThickness,
    colors: CustomTabColors = CustomTabDefaults.customTabColors(),
) {
    CompositionLocalProvider(LocalContentColor provides if (selected) colors.activeColor else colors.inactiveColor) {
        // How to get exact size without recomposition?
        // https://stackoverflow.com/a/73357119/16484891
        // CC BY-SA 4.0
        SubcomposeLayout(modifier) { constraints ->
            val placeables = subcompose("tabContent") {
                CustomTabContent(
                    text = text,
                    icon = icon,
                    modifier = Modifier
                        .clickable { onClick() }
                        .padding(paddingValues)
                )
            }.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

            var maxWidth = 0
            var maxHeight = 0

            placeables.forEach {
                maxWidth += it.width
                maxHeight = it.height
            }

            val indicatorPlaceables = subcompose("indicator") {
                CustomTabIndicator(
                    selected,
                    maxWidth,
                    maxHeight,
                    indicatorPosition,
                    indicatorLength,
                    indicatorThickness,
                )
            }.map { it.measure(constraints) }

            layout(maxWidth, maxHeight) {
                placeables.forEach {
                    it.placeRelative(0, 0)
                }

                indicatorPlaceables.forEach {
                    it.placeRelative(0, 0)
                }
            }
        }
    }
}
