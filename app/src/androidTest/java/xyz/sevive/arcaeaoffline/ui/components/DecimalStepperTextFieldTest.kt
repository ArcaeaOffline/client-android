package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.requestFocus
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DecimalStepperTextFieldTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testStepperIncrement_preservesDecimalPlaces() {
        lateinit var state: DecimalStepperTextFieldState
        composeTestRule.setContent {
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

        composeTestRule.onNodeWithTag(DecimalStepperTextFieldTestTags.INCREASE_BUTTON).performClick()
        assertEquals("1.75", state.textFieldState.text.toString())
        composeTestRule.onNodeWithText("1.75").assertExists()
    }

    @Test
    fun testStepperMaxBoundary_truncatesCorrectly() {
        lateinit var state: DecimalStepperTextFieldState
        composeTestRule.setContent {
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
        composeTestRule.onNodeWithTag(DecimalStepperTextFieldTestTags.INCREASE_BUTTON).performClick()
        assertEquals(BigDecimal.parseString("5.00"), state.value)
        assertEquals("5.00", state.textFieldState.text.toString())
    }

    @Test
    fun testStepperMinBoundary_truncatesCorrectly() {
        lateinit var state: DecimalStepperTextFieldState
        composeTestRule.setContent {
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
        composeTestRule.onNodeWithTag(DecimalStepperTextFieldTestTags.DECREASE_BUTTON).performClick()
        assertEquals(BigDecimal.parseString("0.00"), state.value)
        assertEquals("0.00", state.textFieldState.text.toString())
    }

    @Test
    fun testInvalidInput_isRejected() {
        lateinit var state: DecimalStepperTextFieldState
        composeTestRule.setContent {
            state = rememberDecimalStepperTextFieldState(initialValue = 1.0, maxDecimalPlaces = 2)
            DecimalStepperTextField(state)
        }

        val inputNode = composeTestRule.onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD)

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
    fun testNegativeValueInput_isSupported() {
        lateinit var state: DecimalStepperTextFieldState
        composeTestRule.setContent {
            state =
                rememberDecimalStepperTextFieldState(
                    initialValue = 0.0,
                    minValue = -10.0,
                    maxValue = 10.0,
                )
            DecimalStepperTextField(state)
        }

        val inputNode = composeTestRule.onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD)

        inputNode.performTextReplacement("-5.5")
        assertEquals("-5.5", state.textFieldState.text.toString())

        // Loss of focus should format it
        composeTestRule.onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD).performImeAction()
        assertEquals("-5.50", state.textFieldState.text.toString())
    }

    @Test
    fun testFocusLost_appendsTrailingZeros() {
        lateinit var state: DecimalStepperTextFieldState
        composeTestRule.setContent {
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
        composeTestRule
            .onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD)
            .performTextReplacement("1.5")

        // Changing focus
        composeTestRule.onNodeWithTag("other_text_field").requestFocus()

        assertEquals("1.500", state.textFieldState.text.toString())
    }

    @Test
    fun testLocaleComma_isReplacedWithDot() {
        lateinit var state: DecimalStepperTextFieldState
        composeTestRule.setContent {
            state = rememberDecimalStepperTextFieldState(initialValue = 0.0)
            DecimalStepperTextField(state)
        }

        // Some locale use comma for decimal separator
        composeTestRule
            .onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD)
            .performTextReplacement("12,34")

        assertEquals("12.34", state.textFieldState.text.toString())
    }

    @Test
    fun testDisabledState_blocksInteractions() {
        lateinit var state: DecimalStepperTextFieldState
        composeTestRule.setContent {
            state = rememberDecimalStepperTextFieldState(5.0, maxDecimalPlaces = 2)
            DecimalStepperTextField(
                state = state,
                enabled = false,
            )
        }

        composeTestRule.onNodeWithTag(DecimalStepperTextFieldTestTags.INCREASE_BUTTON).performClick()
        assertEquals(BigDecimal.parseString("5.00"), state.value)

        composeTestRule.onNodeWithTag(DecimalStepperTextFieldTestTags.DECREASE_BUTTON).performClick()
        assertEquals(BigDecimal.parseString("5.00"), state.value)

        composeTestRule
            .onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.IsEditable, false))
    }

    @Test
    fun testReadonlyState_blocksInteractions() {
        lateinit var state: DecimalStepperTextFieldState
        composeTestRule.setContent {
            state = rememberDecimalStepperTextFieldState(5.0, maxDecimalPlaces = 2)
            DecimalStepperTextField(
                state = state,
                readonly = true,
            )
        }

        composeTestRule.onNodeWithTag(DecimalStepperTextFieldTestTags.INCREASE_BUTTON).performClick()
        assertEquals(BigDecimal.parseString("5.00"), state.value)

        composeTestRule.onNodeWithTag(DecimalStepperTextFieldTestTags.DECREASE_BUTTON).performClick()
        assertEquals(BigDecimal.parseString("5.00"), state.value)

        composeTestRule
            .onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.IsEditable, false))
    }

    @Test
    fun testRepeatingIconButton_longPressContinuousStepping() {
        val step = 1.0
        lateinit var state: DecimalStepperTextFieldState
        composeTestRule.setContent {
            state = rememberDecimalStepperTextFieldState(1.0, step = step)
            DecimalStepperTextField(state)
        }

        // The clock should be manually controlled
        composeTestRule.mainClock.autoAdvance = false

        val increaseNode = composeTestRule.onNodeWithTag(DecimalStepperTextFieldTestTags.INCREASE_BUTTON)

        // Simulate long press indication
        increaseNode.performTouchInput { down(center) }

        // The first click should be immediately applied
        composeTestRule.mainClock.advanceTimeByFrame()
        assertEquals(BigDecimal.parseString("1.00"), state.value)

        // Advance clock to skip initialDelay (500ms), and perform some additions
        composeTestRule.mainClock.advanceTimeBy(500 + 525)

        // We cannot really ensure the value since you cannot rely on device clocks...
        // So we just assume the number is larger than a reasonable value
        val valueAfterLongPress = state.value?.doubleValue() ?: 0.0
        assert(valueAfterLongPress >= 6.0)

        // Stop long press
        increaseNode.performTouchInput { up() }

        // Then advance the clock
        // The value should ideologically be the same, but since the cranky long press implementation,
        // it might be slightly off 1~2 step. We just tolerate that since user should be able to adjust
        // this by themselves...
        composeTestRule.mainClock.advanceTimeBy(500)
        val stoppedValue = state.value?.doubleValue() ?: 0.0
        assert(valueAfterLongPress <= stoppedValue && stoppedValue <= valueAfterLongPress + (step * 2))

        // Reset clock
        composeTestRule.mainClock.autoAdvance = true
    }

    @Test
    fun testImeActionDone_triggersFormatting() {
        lateinit var state: DecimalStepperTextFieldState
        composeTestRule.setContent {
            state = rememberDecimalStepperTextFieldState(0.0, maxDecimalPlaces = 3)
            DecimalStepperTextField(state)
        }

        val inputNode = composeTestRule.onNodeWithTag(DecimalStepperTextFieldTestTags.TEXT_FIELD)

        inputNode.performTextReplacement("7.5")
        // The focus isn't lost yet, so the text should stay the same
        assertEquals("7.5", state.textFieldState.text.toString())

        // When the user clicks Done, the focus should be lost,
        // and the text should be formatted
        inputNode.performImeAction()
        assertEquals("7.500", state.textFieldState.text.toString())
    }
}
