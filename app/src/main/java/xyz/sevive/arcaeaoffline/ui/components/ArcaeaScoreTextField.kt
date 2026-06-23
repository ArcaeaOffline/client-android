package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import xyz.sevive.arcaeaoffline.R
import kotlin.math.abs

/**
 * AI Generated - This object is generated mostly by Gemini.
 */
object ArcaeaScoreInputTransformation : InputTransformation {
    private const val FIXED_DIGIT_COUNT = 8

    fun createInitialText(value: Long): String {
        // Avoid negative values
        val rawDigits = abs(value).toString()

        // Lock the digits length
        val normalizedDigits =
            if (rawDigits.length > FIXED_DIGIT_COUNT) {
                // or use takeLast if we encounter any issues later
                rawDigits.take(FIXED_DIGIT_COUNT)
            } else {
                // pad if not enough digits
                rawDigits.padStart(FIXED_DIGIT_COUNT, '0')
            }

        return formatWithThousandsSeparator(normalizedDigits)
    }

    fun createInitialText(value: Int) = createInitialText(value.toLong())

    override fun TextFieldBuffer.transformInput() {
        val text = this.asCharSequence()

        // 1. Count how many raw digits precede the selection start/end in buffer
        val digitsBeforeStart = countDigitsUpTo(text, this.selection.start)
        val digitsBeforeEnd = countDigitsUpTo(text, this.selection.end)

        // 2. Extract all digit characters from the current buffer
        var rawDigits = text.filter { it.isDigit() }.toString()

        // 3. Core constraint: keep rawDigits length exactly to FIXED_DIGIT_COUNT
        if (rawDigits.length > FIXED_DIGIT_COUNT) {
            // Input/paste: too many digits
            // trim the excess digits starting right after the insertion point (digitsBeforeStart)
            val excess = rawDigits.length - FIXED_DIGIT_COUNT
            val removeStart = digitsBeforeStart
            val removeEnd = (removeStart + excess).coerceAtMost(rawDigits.length)

            rawDigits = rawDigits.removeRange(removeStart, removeEnd)

            // Defensive safety trim (e.g. extremely long paste at the end)
            if (rawDigits.length > FIXED_DIGIT_COUNT) {
                rawDigits = rawDigits.take(FIXED_DIGIT_COUNT)
            }
        } else if (rawDigits.length < FIXED_DIGIT_COUNT) {
            // Deletion: too few digits
            // pad with zeros at the deletion position (digitsBeforeStart)
            val missing = FIXED_DIGIT_COUNT - rawDigits.length
            val padIndex = digitsBeforeStart.coerceIn(0, rawDigits.length)
            val padding = "0".repeat(missing)

            rawDigits = StringBuilder(rawDigits).insert(padIndex, padding).toString()
        }

        // 4. Reformat the digit string with ' separators
        val formatted = formatWithThousandsSeparator(rawDigits)

        // 5. Write the formatted text back into the buffer
        this.replace(0, this.length, formatted)

        // 6. Restore cursor/selection precisely
        // by mapping the original digit‑based positions into the formatted text
        val newStart = findPositionForDigits(formatted, digitsBeforeStart)
        val newEnd = findPositionForDigits(formatted, digitsBeforeEnd)
        this.selection = TextRange(newStart, newEnd)
    }

    private fun countDigitsUpTo(
        text: CharSequence,
        index: Int,
    ): Int {
        var count = 0
        val limit = index.coerceAtMost(text.length)
        for (i in 0 until limit) {
            if (text[i].isDigit()) count++
        }
        return count
    }

    private fun formatWithThousandsSeparator(raw: String) =
        buildString {
            raw.forEachIndexed { i, char ->
                append(char)
                val remaining = raw.length - 1 - i
                if (remaining > 0 && remaining % 3 == 0) {
                    append("'")
                }
            }
        }

    private fun findPositionForDigits(
        formatted: String,
        targetDigitCount: Int,
    ): Int {
        var digitCount = 0
        var index = 0
        while (digitCount < targetDigitCount && index < formatted.length) {
            if (formatted[index].isDigit()) {
                digitCount += 1
            }
            index += 1
        }
        return index
    }
}

class ArcaeaScoreTextFieldState(
    val textFieldState: TextFieldState,
) {
    val intValue: Int?
        get() =
            textFieldState.text
                .filter { it.isDigit() }
                .toString()
                .toIntOrNull()

    fun updateValue(newValue: Int) {
        val newText = ArcaeaScoreInputTransformation.createInitialText(newValue)
        textFieldState.edit {
            replace(0, length, newText)
        }
    }
}

@Composable
fun rememberArcaeaScoreTextFieldState(
    initialValue: Int = 0,
    initialSelectAll: Boolean = false,
): ArcaeaScoreTextFieldState {
    val initialText = ArcaeaScoreInputTransformation.createInitialText(initialValue)
    val textFieldState =
        rememberTextFieldState(
            initialText = initialText,
            initialSelection = if (initialSelectAll) TextRange(0, initialText.length) else TextRange(initialText.length),
        )

    return remember(textFieldState) {
        ArcaeaScoreTextFieldState(textFieldState)
    }
}

@Composable
fun OutlinedArcaeaScoreTextField(
    state: ArcaeaScoreTextFieldState,
    modifier: Modifier = Modifier,
    label: @Composable TextFieldLabelScope.() -> Unit = { Text(stringResource(R.string.arcaea_play_result_score)) },
) {
    OutlinedTextField(
        state.textFieldState,
        modifier,
        inputTransformation = ArcaeaScoreInputTransformation,
        label = label,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
    )
}
