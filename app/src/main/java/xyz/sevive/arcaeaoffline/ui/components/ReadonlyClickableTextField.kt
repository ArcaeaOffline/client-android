package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue

/**
 * A readonly `TextField` that can handle user clicks.
 */
@Composable
fun ReadonlyClickableTextField(
    value: TextFieldValue?,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    modifier: Modifier,
) {
    TextField(
        value = value ?: TextFieldValue(),
        onValueChange = {},
        readOnly = true,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        modifier = modifier,
        // detect click in a readonly textfield
        // https://stackoverflow.com/a/75826824/16484891, CC BY-SA 4.0
        interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
            LaunchedEffect(interactionSource) {
                interactionSource.interactions.collect {
                    if (it is PressInteraction.Release) {
                        onClick()
                    }
                }
            }
        },
    )
}
