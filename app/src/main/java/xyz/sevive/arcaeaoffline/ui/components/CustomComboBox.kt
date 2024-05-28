package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomComboBox(
    options: List<Pair<TextFieldValue, Any>>,
    selectedIndex: Int,
    onSelectChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable () -> Unit = {},
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = { Icon(Icons.Default.ArrowDropDown, null) },
    menuAnchorType: MenuAnchorType = MenuAnchorType.PrimaryNotEditable,
) {
    val labels = options.map { it.first }

    var userExpanded by remember { mutableStateOf(false) }
    val expanded = enabled && userExpanded
    val trailingIconRotateDegree by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "trailingIconRotateDegree"
    )

    LaunchedEffect(enabled, userExpanded) {
        if (!enabled) userExpanded = false
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { userExpanded = !userExpanded },
        modifier = modifier,
    ) {
        TextField(
            readOnly = true,
            value = if (selectedIndex > -1) labels[selectedIndex] else TextFieldValue(),
            onValueChange = { },
            enabled = enabled,
            singleLine = true,
            label = { label() },
            leadingIcon = leadingIcon,
            trailingIcon = if (trailingIcon != null) {
                { Box(Modifier.rotate(trailingIconRotateDegree)) { trailingIcon() } }
            } else null,
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(menuAnchorType, enabled),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { userExpanded = false },
        ) {
            options.indices.forEach { i ->
                DropdownMenuItem(
                    onClick = {
                        onSelectChanged(i)
                        userExpanded = false
                    },
                    text = { Text(labels[i].annotatedString) },
                )
            }
        }
    }
}
