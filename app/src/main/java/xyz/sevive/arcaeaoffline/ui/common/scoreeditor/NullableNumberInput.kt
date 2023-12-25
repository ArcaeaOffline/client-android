package xyz.sevive.arcaeaoffline.ui.common.scoreeditor

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.delay


internal fun keepNumInRange(num: Int, minimum: Int? = null, maximum: Int? = null): Int {
    var returnNum = num

    if (minimum != null && num < minimum) {
        returnNum = minimum
    }
    if (maximum != null && num > maximum) {
        returnNum = maximum
    }

    return returnNum
}

internal fun addNum(num: Int, maximum: Int?): Int {
    return if (maximum == null || num < maximum) {
        num + 1
    } else {
        num
    }
}


internal fun minusNum(num: Int, minimum: Int?): Int {
    return if (minimum == null || num > minimum) {
        num - 1
    } else {
        num
    }
}

@Composable
internal fun NullableNumberInput(
    value: Int?,
    onNumberChange: (value: Int) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit = {},
    minimum: Int? = 0,
    maximum: Int? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    var isAutoResetToZero by remember { mutableStateOf(false) }
    val numberText =
        if ((value == 0 && isAutoResetToZero) || value == null) "" else value.toString()

    val editable = value != null

    var oneShotMinus by remember { mutableStateOf(false) }
    var oneShotAdd by remember { mutableStateOf(false) }
    var longPressMinus by remember { mutableStateOf(false) }
    var longPressAdd by remember { mutableStateOf(false) }

    val longPressing by remember { derivedStateOf { longPressAdd || longPressMinus } }

    if (value != null) {
        if (oneShotMinus) {
            LaunchedEffect(1) {
                onNumberChange(minusNum(value, minimum))
                oneShotMinus = false
            }
        }

        if (oneShotAdd) {
            LaunchedEffect(1) {
                onNumberChange(addNum(value, maximum))
                oneShotAdd = false
            }
        }

        if (longPressMinus) {
            LaunchedEffect(value) {
                delay(50L)
                onNumberChange(minusNum(value, minimum))
            }
        }

        if (longPressAdd) {
            LaunchedEffect(value) {
                delay(50L)
                onNumberChange(addNum(value, maximum))
            }
        }
    }


    TextField(
        value = numberText,
        onValueChange = {
            var number = it.toIntOrNull()

            if (number == null) {
                onNumberChange(0)
                isAutoResetToZero = true
            } else {
                number = keepNumInRange(number, minimum, maximum)

                if (it == "0" && isAutoResetToZero) {
                    isAutoResetToZero = false
                    onNumberChange(0)
                } else {
                    onNumberChange(number)
                }
            }
        },
        modifier = modifier,
        label = label,
        leadingIcon = {
            val interactionSource = remember { MutableInteractionSource() }

            Icon(
                Icons.Default.Remove, null,
                modifier = if (editable) {
                    Modifier
                        .indication(interactionSource, LocalIndication.current)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { oneShotMinus = true },
                                onLongPress = { longPressMinus = true },
                                onPress = {
                                    // add ripple effect to custom gesture
                                    // https://stackoverflow.com/a/69754118/16484891
                                    // CC BY-SA 4.0
                                    val press = PressInteraction.Press(it)
                                    interactionSource.emit(press)

                                    tryAwaitRelease()

                                    interactionSource.emit(PressInteraction.Release(press))
                                    longPressMinus = false
                                },
                            )
                        }
                } else {
                    Modifier
                },
            )
        },
        trailingIcon = {
            val interactionSource = remember { MutableInteractionSource() }

            Icon(
                Icons.Default.Add,
                null,
                modifier = if (editable) {
                    Modifier
                        .indication(interactionSource, LocalIndication.current)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { oneShotAdd = true },
                                onLongPress = { longPressAdd = true },
                                onPress = {
                                    val press = PressInteraction.Press(it)
                                    interactionSource.emit(press)

                                    tryAwaitRelease()

                                    interactionSource.emit(PressInteraction.Release(press))
                                    longPressAdd = false
                                },
                            )
                        }
                } else {
                    Modifier
                },
            )
        },
        placeholder = { Text("0") },
        enabled = editable && !longPressing,
        colors = if (longPressAdd) {
            TextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (longPressMinus) {
            TextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else TextFieldDefaults.colors(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = visualTransformation,
    )
}
