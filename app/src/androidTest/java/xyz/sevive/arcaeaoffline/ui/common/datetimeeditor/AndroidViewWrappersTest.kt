package xyz.sevive.arcaeaoffline.ui.common.datetimeeditor

import android.widget.CalendarView
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidViewWrappersTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun datePicker_initializationAndSelectionAreCorrect() {
        var capturedDate: LocalDate? = null

        composeTestRule.setContent {
            var selectedDate by remember { mutableStateOf(LocalDate(2026, 6, 16)) }

            AndroidViewDatePickerDialog(
                date = selectedDate,
                onDateSelect = {
                    capturedDate = it
                    selectedDate = it
                },
            )
        }

        // Assert initialization
        onView(isAssignableFrom(DatePicker::class.java)).check { view, _ ->
            val datePicker = view as DatePicker
            assertEquals(2026, datePicker.year)
            assertEquals(6 - 1, datePicker.month)
            assertEquals(16, datePicker.dayOfMonth)
        }

        // Simulate user interactions
        // Note that Espresso has adjustments to the month (no minus 1 required)
        onView(isAssignableFrom(DatePicker::class.java))
            .perform(PickerActions.setDate(2026, 2, 6))

        // Assert selection
        composeTestRule.runOnIdle {
            assertNotNull(capturedDate)
            assertEquals(2026, capturedDate?.year)
            assertEquals(2, capturedDate?.month?.number)
            assertEquals(6, capturedDate?.day)
        }
    }

    @Test
    fun calendarView_initializationIsCorrect() {
        val initialDate = LocalDate(2026, 6, 19)

        composeTestRule.setContent {
            AndroidViewCalendar(
                date = initialDate,
                onDateSelect = {},
            )
        }

        onView(isAssignableFrom(CalendarView::class.java)).check { view, _ ->
            val calendarView = view as CalendarView
            val expectedMillis = initialDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

            assertEquals(expectedMillis, calendarView.date)
        }
    }

    @Test
    fun timePicker_initializationAndSelectionAreCorrect() {
        var capturedTime: LocalTime? = null

        composeTestRule.setContent {
            var selectedTime by remember { mutableStateOf(LocalTime(12, 34)) }

            AndroidViewTimePicker(
                time = selectedTime,
                onTimeSelect = {
                    capturedTime = it
                    selectedTime = it
                },
            )
        }

        // Assert initialization
        onView(isAssignableFrom(TimePicker::class.java)).check { view, _ ->
            val timePicker = view as TimePicker
            assertEquals(12, timePicker.hour)
            assertEquals(34, timePicker.minute)
        }

        // Simulate user interactions
        onView(isAssignableFrom(TimePicker::class.java))
            .perform(PickerActions.setTime(21, 43))

        // Assert selection
        composeTestRule.runOnIdle {
            assertNotNull(capturedTime)
            assertEquals(21, capturedTime?.hour)
            assertEquals(43, capturedTime?.minute)
        }
    }
}
