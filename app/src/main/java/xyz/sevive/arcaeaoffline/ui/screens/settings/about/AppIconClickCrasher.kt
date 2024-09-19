package xyz.sevive.arcaeaoffline.ui.screens.settings.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


internal class AppIconClickCrasherState(
    clicksToCrash: Int,
    private val coroutineScope: CoroutineScope
) {
    private var delayedResetJob: Job? = null

    var count by mutableIntStateOf(clicksToCrash)
        private set

    fun clicked() {
        delayedResetJob?.cancel()
        count -= 1

        if (count <= 0) throw AppIconBeingClickedTooManyTimesSoEmbarrassingException()

        delayedResetJob = coroutineScope.launch {
            delay(1500L)
            count = 7
        }
    }
}

@Composable
internal fun rememberAppIconClickCrasherState(
    clicksToCrash: Int = 7,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): AppIconClickCrasherState {
    return remember { AppIconClickCrasherState(clicksToCrash, coroutineScope) }
}
