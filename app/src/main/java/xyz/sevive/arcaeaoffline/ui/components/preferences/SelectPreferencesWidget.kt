package xyz.sevive.arcaeaoffline.ui.components.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha

data class SelectPreferencesOption<T>(
    val value: T,
    val label: String,
    val description: String? = null,
)

/**
 * A radio-button list of mutually exclusive options; renders one
 * [BasePreferencesWidget] row per option without an outer container.
 */
@Composable
fun <T> SelectPreferencesWidget(
    options: List<SelectPreferencesOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        options.forEach { option ->
            BasePreferencesWidget(
                onClick = { onSelect(option.value) },
                title = { Text(option.label) },
                content = option.description?.let { description ->
                    {
                        Text(
                            description,
                            Modifier.secondaryItemAlpha(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                leadingSlot = {
                    RadioButton(
                        selected = option.value == selected,
                        onClick = null,
                    )
                },
            )
        }
    }
}
