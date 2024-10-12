package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput

/* https://stackoverflow.com/a/69146178/16484891, CC BY-SA 4.0 */
private fun Modifier.interceptChildrenInteractions(intercept: Boolean = true) =
    if (intercept) {
        pointerInput(Unit) {
            awaitPointerEventScope {
                // we should wait for all new pointer events
                while (true) {
                    awaitPointerEvent(pass = PointerEventPass.Initial)
                        .changes
                        .forEach(PointerInputChange::consume)
                }
            }
        }
    } else {
        this
    }

@Composable
fun LoadingOverlay(
    loading: Boolean,
    modifier: Modifier = Modifier,
    interceptInteractionWhenLoading: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val overlayBackgroundAlpha by animateFloatAsState(
        targetValue = if (loading) 0.9f else 0f,
        label = "overlayBackgroundAlpha",
    )
    val shouldShowOverlay by remember {
        derivedStateOf { overlayBackgroundAlpha > 0f }
    }
    val shouldInterceptInteraction = loading && interceptInteractionWhenLoading

    Box(
        Modifier
            .interceptChildrenInteractions(intercept = shouldInterceptInteraction)
            .then(modifier)
    ) {
        content()

        if (shouldShowOverlay) {
            Box(
                Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = overlayBackgroundAlpha))
                    .fillMaxSize()
            ) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}
