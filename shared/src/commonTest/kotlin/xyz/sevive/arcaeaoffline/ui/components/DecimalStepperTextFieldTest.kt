package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.experimental.categories.Category
import xyz.sevive.arcaeaoffline.test.category.AndroidHostTestIncompatible
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
@Category(AndroidHostTestIncompatible::class)
class DecimalStepperTextFieldTest {
    @Test
    fun stepping_up_preserves_decimal_places() =
        runComposeUiTest {
            lateinit var state: DecimalStepperTextFieldState
            setContent {
                state =
                    rememberDecimalStepperTextFieldState(
                        initialValue = 1.5,
                        maxDecimalPlaces = 2,
                        step = 0.25,
                        minValue = 0.0,
                        maxValue = 5.0,
                    )
                DecimalStepperTextField(state)
            }

            assertEquals("1.50", state.textFieldState.text.toString())

            onNodeWithTag(DecimalStepperTextFieldTestTags.INCREASE_BUTTON).performClick()
            assertEquals("1.75", state.textFieldState.text.toString())
            onNodeWithText("1.75").assertExists()
        }

    @Test
    fun stepping_past_max_boundary_clamps_to_max_value() =
        runComposeUiTest {
            lateinit var state: DecimalStepperTextFieldState
            setContent {
                state =
                    rememberDecimalStepperTextFieldState(
                        initialValue = 4.9,
                        maxDecimalPlaces = 2,
                        step = 0.5,
                        minValue = 0.0,
                        maxValue = 5.0,
                    )
                DecimalStepperTextField(state)
            }

            // 4.90 + 0.50 = 5.40, BUT it should be truncated to maxValue 5.00
            onNodeWithTag(DecimalStepperTextFieldTestTags.INCREASE_BUTTON).performClick()
            assertEquals(BigDecimal.parseString("5.00"), state.value)
            assertEquals("5.00", state.textFieldState.text.toString())
        }

    @Test
    fun stepping_past_min_boundary_clamps_to_min_value() =
        runComposeUiTest {
            lateinit var state: DecimalStepperTextFieldState
            setContent {
                state =
                    rememberDecimalStepperTextFieldState(
                        initialValue = 0.1,
                        maxDecimalPlaces = 2,
                        step = 0.5,
                        minValue = 0.0,
                        maxValue = 5.0,
                    )
                DecimalStepperTextField(state)
            }

            // 0.10 - 0.50 = -0.40, BUT it should be truncated to minValue 0.00
            onNodeWithTag(DecimalStepperTextFieldTestTags.DECREASE_BUTTON).performClick()
            assertEquals(BigDecimal.parseString("0.00"), state.value)
            assertEquals("0.00", state.textFieldState.text.toString())
        }

    @Test
    fun rejects_invalid_inputs() =
        runComposeUiTest {
            lateinit var state: DecimalStepperTextFieldState
            setContent {
                state = rememberDecimalStepperTextFieldState(initialValue = 1.0, maxDecimalPlaces = 2)
                DecimalStepperTextField(state)
            }

            val inputNode = onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD)

            // Try to input letters
            inputNode.performTextReplacement("abc")
            assertEquals("1.00", state.textFieldState.text.toString())

            // Try to input multiple dots
            inputNode.performTextReplacement("1.2.3")
            assertEquals("1.00", state.textFieldState.text.toString())

            // Try to input more decimal places than allowed
            inputNode.performTextReplacement("1.234")
            assertEquals("1.00", state.textFieldState.text.toString())
        }

    @Test
    fun allows_negative_input() =
        runComposeUiTest {
            lateinit var state: DecimalStepperTextFieldState
            setContent {
                state =
                    rememberDecimalStepperTextFieldState(
                        initialValue = 0.0,
                        minValue = -10.0,
                        maxValue = 10.0,
                    )
                DecimalStepperTextField(state)
            }

            val inputNode = onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD)

            inputNode.performTextReplacement("-5.5")
            assertEquals("-5.5", state.textFieldState.text.toString())

            // Loss of focus should format it
            onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD).performImeAction()
            assertEquals("-5.50", state.textFieldState.text.toString())
        }

    @Test
    fun losing_focus_appends_trailing_zeros() =
        runComposeUiTest {
            lateinit var state: DecimalStepperTextFieldState
            setContent {
                state =
                    rememberDecimalStepperTextFieldState(
                        initialValue = 1.5,
                        maxDecimalPlaces = 3,
                    )

                Column {
                    DecimalStepperTextField(state)
                    // A clickable item to change focus
                    TextField(
                        value = "",
                        onValueChange = {},
                        Modifier.testTag("other_text_field"),
                    )
                }
            }

            // Input value that should be normalized
            onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD).performTextReplacement("1.5")

            // Changing focus
            onNodeWithTag("other_text_field").requestFocus()

            assertEquals("1.500", state.textFieldState.text.toString())
        }

    @Test
    fun defaulting_to_minimum_value_when_focus_lost_with_empty_input() =
        runComposeUiTest {
            lateinit var state: DecimalStepperTextFieldState
            setContent {
                state =
                    rememberDecimalStepperTextFieldState(
                        initialValue = 1.5,
                        maxDecimalPlaces = 2,
                        minValue = 1.0,
                    )

                Column {
                    DecimalStepperTextField(state)
                    // A clickable item to change focus
                    TextField(
                        value = "",
                        onValueChange = {},
                        Modifier.testTag("other_text_field"),
                    )
                }
            }

            onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD).performTextClearance()
            // Changing focus
            onNodeWithTag("other_text_field").requestFocus()

            assertEquals("1.00", state.textFieldState.text.toString())
        }

    @Test
    fun replaces_locale_comma_with_dot() =
        runComposeUiTest {
            lateinit var state: DecimalStepperTextFieldState
            setContent {
                state = rememberDecimalStepperTextFieldState(initialValue = 0.0)
                DecimalStepperTextField(state)
            }

            // Some locale use comma for decimal separator
            onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD).performTextReplacement("12,34")

            assertEquals("12.34", state.textFieldState.text.toString())
        }

    @Test
    fun disabled_state_blocks_interactions() =
        runComposeUiTest {
            lateinit var state: DecimalStepperTextFieldState
            setContent {
                state = rememberDecimalStepperTextFieldState(5.0, maxDecimalPlaces = 2)
                DecimalStepperTextField(
                    state = state,
                    enabled = false,
                )
            }

            onNodeWithTag(DecimalStepperTextFieldTestTags.INCREASE_BUTTON).assertDoesNotExist()
            onNodeWithTag(DecimalStepperTextFieldTestTags.DECREASE_BUTTON).assertDoesNotExist()

            onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD).assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.IsEditable,
                    false,
                ),
            )
        }

    @Test
    fun readonly_state_blocks_interactions() =
        runComposeUiTest {
            lateinit var state: DecimalStepperTextFieldState
            setContent {
                state = rememberDecimalStepperTextFieldState(5.0, maxDecimalPlaces = 2)
                DecimalStepperTextField(
                    state = state,
                    readonly = true,
                )
            }

            onNodeWithTag(DecimalStepperTextFieldTestTags.INCREASE_BUTTON).assertDoesNotExist()
            onNodeWithTag(DecimalStepperTextFieldTestTags.DECREASE_BUTTON).assertDoesNotExist()

            onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD).assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.IsEditable,
                    false,
                ),
            )
        }

    @Test
    fun ime_done_action_triggers_formatting() =
        runComposeUiTest {
            lateinit var state: DecimalStepperTextFieldState
            setContent {
                state = rememberDecimalStepperTextFieldState(0.0, maxDecimalPlaces = 3)
                DecimalStepperTextField(state)
            }

            val inputNode = onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD)

            inputNode.performTextReplacement("7.5")
            // The focus isn't lost yet, so the text should stay the same
            assertEquals("7.5", state.textFieldState.text.toString())

            // When the user clicks Done, the focus should be lost,
            // and the text should be formatted
            inputNode.performImeAction()
            assertEquals("7.500", state.textFieldState.text.toString())
        }
}
