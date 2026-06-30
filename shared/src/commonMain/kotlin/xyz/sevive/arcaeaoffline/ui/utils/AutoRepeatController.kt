package xyz.sevive.arcaeaoffline.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalAtomicApi::class)
@Stable
class AutoRepeatController internal constructor(
    private val scope: CoroutineScope,
    private val initialDelay: Duration,
    private val repeatDelay: Duration,
) {
    private val repeatJob = AtomicReference<Job?>(null)

    /**
     * Call when a press begins.
     *
     * Executes [action] immediately, then repeats it after [initialDelay].
     */
    fun press(action: () -> Unit) {
        // Ignore auto-repeat KeyDown or duplicated presses.
        if (repeatJob.load() != null) return

        action()

        repeatJob.store(
            scope.launch {
                delay(initialDelay)

                while (true) {
                    action()
                    delay(repeatDelay)
                }
            },
        )
    }

    /**
     * Call when the press ends.
     */
    fun release() {
        repeatJob.load()?.cancel()
        repeatJob.store(null)
    }

    /**
     * Cancel any active repeat.
     */
    fun cancel() = release()
}

@Composable
fun rememberAutoRepeatController(
    initialDelay: Duration = 500.milliseconds,
    repeatDelay: Duration = 50.milliseconds,
): AutoRepeatController {
    val scope = rememberCoroutineScope()

    val controller =
        remember(scope, initialDelay, repeatDelay) {
            AutoRepeatController(
                scope = scope,
                initialDelay = initialDelay,
                repeatDelay = repeatDelay,
            )
        }

    DisposableEffect(controller) {
        onDispose {
            controller.cancel()
        }
    }

    return controller
}
