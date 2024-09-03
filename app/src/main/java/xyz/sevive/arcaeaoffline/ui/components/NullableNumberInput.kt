package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp


@Composable
private fun NullableNumberInputEditDialog(
    onDismiss: () -> Unit,
    value: Int,
    onValueChange: (value: Int) -> Unit,
    modifier: Modifier = Modifier,
    minimum: Int = 0,
    maximum: Int = Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val onNumberChangeCoerceIn = remember(minimum, maximum, onValueChange) {
        { num: Int -> num.coerceIn(minimum, maximum).let(onValueChange) }
    }

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(value.toString()))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    modifier = Modifier
                        .testTag("nullableNumberInputTextField")
                        .then(modifier),
                    placeholder = { Text("0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = visualTransformation,
                )

            }
        },
        confirmButton = {
            FilledIconButton(onClick = {
                onNumberChangeCoerceIn(textFieldValue.text.toIntOrNull() ?: 0)
                onDismiss()
            }) {
                Icon(Icons.Default.Check, contentDescription = null)
            }
        },
        dismissButton = {
            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
    )
}

@Composable
fun NullableNumberInput(
    value: Int?,
    onValueChange: (value: Int) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit = {},
    minimum: Int = 0,
    maximum: Int = Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val textFieldValue = remember(value) { TextFieldValue(value?.toString() ?: "") }
    val editable = remember(value) { value != null }

    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    if (showEditDialog && value != null) {
        NullableNumberInputEditDialog(
            onDismiss = { showEditDialog = false },
            value = value,
            onValueChange = onValueChange,
            minimum = minimum,
            maximum = maximum,
            visualTransformation = visualTransformation,
        )
    }

    val tryOpenEditDialog = remember(editable) { { if (editable) showEditDialog = true } }

    TextField(
        value = textFieldValue,
        onValueChange = { },
        modifier = Modifier
            .clickable(editable) { tryOpenEditDialog() }
            .then(modifier),
        readOnly = true,
        enabled = editable,
        label = label,
        trailingIcon = {
            IconButton(onClick = tryOpenEditDialog, enabled = editable) {
                Icon(Icons.Default.Edit, contentDescription = null)
            }
        },
        singleLine = true,
        visualTransformation = visualTransformation,
    )
}
