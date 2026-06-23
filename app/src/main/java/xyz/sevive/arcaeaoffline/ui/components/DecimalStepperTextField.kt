package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.delay
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object DecimalStepperTextFieldTestTags {
    const val DECREASE_BUTTON = "DecimalStepperTextField_DecreaseIconButton"
    const val TEXT_FIELD = "DecimalStepperTextField_TextField"
    const val INCREASE_BUTTON = "DecimalStepperTextField_IncreaseIconButton"
}

/**
 * AI Generated - This class is generated mostly by Gemini.
 */
class DecimalStepperTextFieldState(
    val textFieldState: TextFieldState,
    val maxDecimalPlaces: Int = 2,
    val step: BigDecimal = BigDecimal.ONE,
    val minValue: BigDecimal = BigDecimal.fromDouble(Double.MIN_VALUE),
    val maxValue: BigDecimal = BigDecimal.fromDouble(Double.MAX_VALUE),
) {
    companion object {
        fun normalizeValue(
            newValue: BigDecimal,
            maxDecimalPlaces: Int,
        ) = newValue
            .scale(maxDecimalPlaces.toLong())
            .roundToDigitPositionAfterDecimalPoint(
                maxDecimalPlaces.toLong(),
                RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
            )
    }

    val value: BigDecimal?
        get() = runCatching { textFieldState.text.toString().toBigDecimal() }.getOrNull()

    val doubleValue
        get() = value?.doubleValue(exactRequired = false)

    fun stepUp() {
        val current = value ?: BigDecimal.ZERO
        commitValue(current + step)
    }

    fun stepDown() {
        val current = value ?: BigDecimal.ZERO
        commitValue(current - step)
    }

    /**
     * This method handles boundary constraints, formatting, and avoiding unnecessary recompositions.
     */
    fun commitValue(newValue: BigDecimal) {
        var clampedValue = newValue

        if (clampedValue < minValue) clampedValue = minValue
        if (clampedValue > maxValue) clampedValue = maxValue

        val newText = normalizeValue(clampedValue, maxDecimalPlaces).toPlainString()

        if (textFieldState.text.toString() != newText) {
            textFieldState.edit { replace(0, length, newText) }
        }
    }

    fun commitValue(newValue: Double) = commitValue(newValue.toBigDecimal())
}

/**
 * AI Generated - This class is generated mostly by Gemini.
 */
class DecimalInputTransformation(
    maxDecimalPlaces: Int,
    private val maxValue: BigDecimal,
    private val minValue: BigDecimal,
) : InputTransformation {
    // Allowed input format:
    // optional negative sign, digits, optional decimal point,
    // and up to maxDecimalPlaces digits after the decimal point.
    private val inputRegex = """^-?\d*(\.\d{0,$maxDecimalPlaces})?$""".toRegex()

    override val keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    override fun TextFieldBuffer.transformInput() {
        // some regions use comma as decimal separator
        // TODO: proper internationalization
        val hasComma = this.toString().contains(",")
        if (hasComma) {
            val correctedText = this.toString().replace(",", ".")
            this.replace(0, this.length, correctedText)
        }

        val text = this.toString()

        // Allow intermediate input status (cleared, negative sign, decimal point, etc.)
        if (text.isEmpty() || text == "-" || text == "." || text == "-.") {
            // If the minValue >= 0, then negative sign should be rejected
            if (minValue >= BigDecimal.ZERO && text.startsWith("-")) {
                this.revertAllChanges()
            }
            return
        }

        // Test input format
        if (!text.matches(inputRegex)) {
            this.revertAllChanges()
            return
        }

        // Verify maxValue when input is valid
        val parsedNumber = runCatching { text.toBigDecimal() }.getOrNull()
        if (parsedNumber != null) {
            if (parsedNumber > maxValue) {
                this.revertAllChanges()
            }

            // Note: ignore minValue here due to possible intermediate status
            // For example, if minValue is 10, and user wants 15.
            // When `1` is input, the minValue restriction, if applied here, will immediately reject it.
            // So we delegate the validation to focus lost, stepping, etc.
        }
    }
}

@Composable
fun rememberDecimalStepperTextFieldState(
    initialValue: BigDecimal,
    maxDecimalPlaces: Int = 2,
    step: BigDecimal = BigDecimal.parseString("0.5"),
    minValue: BigDecimal = BigDecimal.fromDouble(Double.MIN_VALUE),
    maxValue: BigDecimal = BigDecimal.fromDouble(Double.MAX_VALUE),
): DecimalStepperTextFieldState {
    val textFieldState =
        rememberTextFieldState(
            initialText = DecimalStepperTextFieldState.normalizeValue(initialValue, maxDecimalPlaces).toPlainString(),
        )

    val state =
        remember(textFieldState, maxDecimalPlaces, step, minValue, maxValue) {
            DecimalStepperTextFieldState(
                textFieldState = textFieldState,
                maxDecimalPlaces = maxDecimalPlaces,
                step = step,
                minValue = minValue,
                maxValue = maxValue,
            )
        }

    return state
}

@Composable
fun rememberDecimalStepperTextFieldState(
    initialValue: Double,
    maxDecimalPlaces: Int = 2,
    step: Double = 0.5,
    minValue: Double = Double.MIN_VALUE,
    maxValue: Double = Double.MAX_VALUE,
) = rememberDecimalStepperTextFieldState(
    initialValue = initialValue.toBigDecimal(),
    maxDecimalPlaces = maxDecimalPlaces,
    step = step.toBigDecimal(),
    minValue = minValue.toBigDecimal(),
    maxValue = maxValue.toBigDecimal(),
)

@Composable
fun rememberArcaeaConstantStepperTextFieldState(initialValue: Double) =
    rememberDecimalStepperTextFieldState(
        initialValue = initialValue,
        maxDecimalPlaces = 1,
        step = 0.1,
        minValue = 0.0,
        maxValue = 99.0,
    )

@Composable
private fun RepeatingIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    initialDelayMillis: Duration = 500.milliseconds,
    repeatDelayMillis: Duration = 80.milliseconds,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed, enabled) {
        if (isPressed && enabled) {
            // wait for a period to confirm the long click indication
            delay(initialDelayMillis)
            while (true) {
                onClick()
                delay(repeatDelayMillis)
            }
        }
    }

    IconButton(
        onClick = onClick, // this handles the first click interaction
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
fun DecimalStepperTextField(
    state: DecimalStepperTextFieldState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readonly: Boolean = false,
    label: (@Composable TextFieldLabelScope.() -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current

    // Cache InputTransformation to prevent recomposition on each call
    val inputTransformation =
        remember(state.maxDecimalPlaces, state.minValue, state.maxValue) {
            DecimalInputTransformation(
                maxDecimalPlaces = state.maxDecimalPlaces,
                minValue = state.minValue,
                maxValue = state.maxValue,
            )
        }

    val isSteppingEnabled = enabled && !readonly

    OutlinedTextField(
        state.textFieldState,
        modifier =
            modifier
                .testTag(DecimalStepperTextFieldTestTags.TEXT_FIELD)
                // Normalize input when focus is lost
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) return@onFocusChanged
                    state.commitValue(state.value ?: BigDecimal.ZERO)
                },
        inputTransformation = inputTransformation,
        lineLimits = TextFieldLineLimits.SingleLine,
        enabled = enabled,
        readOnly = readonly,
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
        onKeyboardAction = {
            // Clear focus so onFocusChanged will be called
            focusManager.clearFocus()
        },
        label = label,
        leadingIcon =
            if (isSteppingEnabled) {
                {
                    RepeatingIconButton(
                        onClick = { state.stepDown() },
                        modifier = Modifier.testTag(DecimalStepperTextFieldTestTags.DECREASE_BUTTON),
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                }
            } else {
                null
            },
        trailingIcon =
            if (isSteppingEnabled) {
                {
                    RepeatingIconButton(
                        onClick = { state.stepUp() },
                        modifier = Modifier.testTag(DecimalStepperTextFieldTestTags.INCREASE_BUTTON),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            } else {
                null
            },
    )
}

@Composable
@Preview
private fun DecimalStepperTextFieldPreview() {
    val state =
        rememberDecimalStepperTextFieldState(
            initialValue = 0.0,
            maxDecimalPlaces = 2,
            step = 0.5,
            minValue = -100.0,
            maxValue = 100.0,
        )

    ArcaeaOfflineTheme {
        Surface {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DecimalStepperTextField(state)

                Text("state.value is ${state.value}")
            }
        }
    }
}
