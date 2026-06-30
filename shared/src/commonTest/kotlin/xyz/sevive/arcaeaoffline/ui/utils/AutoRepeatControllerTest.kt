package xyz.sevive.arcaeaoffline.ui.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class AutoRepeatControllerTest {
    private val initialDelay = 500.milliseconds
    private val repeatDelay = 50.milliseconds

    private fun TestScope.advanceTimeThenRunCurrent(delayTime: Duration) {
        advanceTimeBy(delayTime)
        runCurrent()
    }

    @Test
    fun press_invokes_action_immediately() =
        runTest {
            var counter = 0
            val controller = AutoRepeatController(backgroundScope, initialDelay, repeatDelay)

            controller.press { counter++ }
            assertEquals(1, counter, "press should invoke action immediately")
        }

    @Test
    fun press_starts_repeating_after_initial_delay() =
        runTest {
            var counter = 0
            val controller = AutoRepeatController(backgroundScope, initialDelay, repeatDelay)

            controller.press { counter++ }
            // Repeat should not start since initialDelay isn't reached
            advanceTimeThenRunCurrent(initialDelay - 1.milliseconds)
            assertEquals(1, counter)

            // Exactly at the initialDelay, second invocation expected
            advanceTimeThenRunCurrent(1.milliseconds)
            assertEquals(2, counter)

            // Repeat should start now
            advanceTimeThenRunCurrent(repeatDelay)
            assertEquals(3, counter)
        }

    @Test
    fun repeated_press_calls_are_ignored() =
        runTest {
            var counter = 0
            val controller = AutoRepeatController(backgroundScope, initialDelay, repeatDelay)

            controller.press { counter++ }
            // Second press before the initialDelay should be ignored
            controller.press { counter++ }
            assertEquals(1, counter)

            // Repeat should start after the initialDelay
            advanceTimeThenRunCurrent(initialDelay)
            assertEquals(2, counter)

            // Additional presses during repeat should still be ignored
            controller.press { counter++ }
            advanceTimeThenRunCurrent(repeatDelay)
            assertEquals(3, counter)
        }

    @Test
    fun release_stops_repeating() =
        runTest {
            var counter = 0
            val controller = AutoRepeatController(backgroundScope, initialDelay, repeatDelay)

            controller.press { counter++ }
            advanceTimeThenRunCurrent(initialDelay)
            assertEquals(2, counter) // initial + first repeat

            controller.release()
            // Counter should stop increasing after releasing
            advanceTimeThenRunCurrent(repeatDelay * 3)
            assertEquals(2, counter)
        }

    @Test
    fun cancel_stops_repeating() =
        runTest {
            var counter = 0
            val controller = AutoRepeatController(backgroundScope, initialDelay, repeatDelay)

            controller.press { counter++ }
            advanceTimeThenRunCurrent(initialDelay)
            assertEquals(2, counter)

            controller.cancel()
            advanceTimeThenRunCurrent(repeatDelay * 3)
            assertEquals(2, counter)
        }

    @Test
    fun release_during_initial_delay_prevents_any_repeat() =
        runTest {
            var counter = 0
            val controller = AutoRepeatController(backgroundScope, initialDelay, repeatDelay)

            controller.press { counter++ }
            // Release before the initialDelay has elapsed
            advanceTimeThenRunCurrent(200.milliseconds)
            controller.release()

            // Forward beyond the initialDelay
            advanceTimeThenRunCurrent(400.milliseconds)
            assertEquals(1, counter, "cancelling during initialDelay should prevent any repeat")
        }

    @Test
    fun controller_can_be_reused_after_release() =
        runTest {
            var counter = 0
            val controller = AutoRepeatController(backgroundScope, initialDelay, repeatDelay)

            // First press-release cycle
            controller.press { counter++ }
            advanceTimeThenRunCurrent(initialDelay + repeatDelay)
            assertTrue(counter > 1)
            controller.release()
            val afterFirstRelease = counter

            // Second press-release cycle
            controller.press { counter++ }
            assertEquals(afterFirstRelease + 1, counter, "second press should invoke action immediately")
            advanceTimeThenRunCurrent(initialDelay)
            assertEquals(afterFirstRelease + 2, counter)
            controller.release()
        }

    @Test
    fun `multiple_rapid_press-release_cycles_work`() =
        runTest {
            var counter = 0
            val controller = AutoRepeatController(backgroundScope, initialDelay, repeatDelay)

            // Rapid tapping (press followed immediately by release)
            repeat(5) {
                controller.press { counter++ }
                controller.release()
            }
            // Each press should invoke the action once, without repeats
            assertEquals(5, counter)
            // Ensure no orphaned coroutines
            advanceUntilIdle()
            assertEquals(5, counter)
        }
}
