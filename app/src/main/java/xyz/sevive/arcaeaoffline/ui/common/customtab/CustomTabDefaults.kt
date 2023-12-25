package xyz.sevive.arcaeaoffline.ui.common.customtab

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class CustomTabColors(
    val activeColor: Color = Color.Unspecified,
    val inactiveColor: Color = Color.Unspecified,
)

object CustomTabDefaults {
    val paddingValues: PaddingValues = PaddingValues(20.dp)

    val indicatorPosition: CustomTabIndicatorPosition = CustomTabIndicatorPosition.BOTTOM
    val indicatorLength: Dp = 20.dp
    val indicatorThickness: Dp = DividerDefaults.Thickness * 3

    @Composable
    fun customTabColors(
        activeColor: Color = MaterialTheme.colorScheme.primary,
        inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ): CustomTabColors {
        return CustomTabColors(activeColor, inactiveColor)
    }
}
