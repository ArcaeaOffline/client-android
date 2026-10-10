package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.theme.spacing
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Result state of an async list-item operation for [OperationTextItem].
 */
sealed interface OperationState {
    data object InProgress : OperationState

    data class Success(
        val message: String? = null,
    ) : OperationState

    data class Failed(
        val message: String? = null,
    ) : OperationState
}

private val RESULT_ICON_HOLD_DURATION = 3.seconds

private val RESULT_ICON_FADE_DURATION = 500.milliseconds

/**
 * A [TextItem] that displays the result of an asynchronous operation.
 *
 * The trailing slot is a loading indicator or success/failure icon based on [state].
 * The [state] message (e.g. [OperationState.Success.message]) will override [content]
 * if provided.
 *
 * To prevent accidental touches (re-entry prevention) and give better result feedback,
 * [onClick] is ignored in the following two cases:
 * - [state] is [OperationState.InProgress].
 * - During the [cooldown] period after the operation finishes.
 *
 * @param state Current operation state. When `null`, no status feedback is shown.
 * @param content Persistent row description, shown only when the state carries no message.
 * @param cooldown Click cooldown duration after the operation finishes.
 * @param onClick Click callback. Ignored during the cooldown and when the operation is in progress.
 */
@Composable
fun OperationTextItem(
    title: String,
    state: OperationState?,
    modifier: Modifier = Modifier,
    content: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    cooldown: Duration = 1.seconds,
    onClick: (() -> Unit)? = null,
) {
    var coolingDown by remember { mutableStateOf(false) }
    var iconSlotVisible by remember { mutableStateOf(false) }
    var iconOpaque by remember { mutableStateOf(false) }
    val iconAlpha by
        animateFloatAsState(
            targetValue = if (iconOpaque) 1f else 0f,
            animationSpec = tween(durationMillis = RESULT_ICON_FADE_DURATION.inWholeMilliseconds.toInt()),
            label = "operationResultIconAlpha",
        )

    LaunchedEffect(state) {
        when (state) {
            is OperationState.InProgress -> {
                iconSlotVisible = true
                iconOpaque = true
                coolingDown = true
            }

            is OperationState.Success, is OperationState.Failed -> {
                iconSlotVisible = true
                iconOpaque = true
                coolingDown = true

                delay(cooldown)
                coolingDown = false

                delay((RESULT_ICON_HOLD_DURATION - cooldown).coerceAtLeast(0.seconds))
                iconOpaque = false
                delay(RESULT_ICON_FADE_DURATION)
                iconSlotVisible = false
            }

            else -> {
                iconSlotVisible = false
                iconOpaque = false
                coolingDown = false
            }
        }
    }

    val clickable =
        onClick != null && enabled &&
            state !is OperationState.InProgress &&
            !coolingDown

    val stateContent =
        when (state) {
            is OperationState.InProgress -> stringResource(R.string.general_please_wait)
            is OperationState.Success -> state.message
            is OperationState.Failed -> state.message ?: stringResource(R.string.general_unknown_error)
            null -> null
        }

    TextItem(
        title = title,
        content = stateContent ?: content,
        leadingSlot =
            leadingIcon?.let { icon ->
                {
                    Icon(icon, contentDescription = null, tint = leadingIconTint)
                }
            },
        enabled = enabled,
        onClick = if (clickable) onClick else null,
        modifier = modifier,
        trailingSlot = {
            if (iconSlotVisible) {
                when (state) {
                    is OperationState.InProgress -> {
                        CircularProgressIndicator(Modifier.size(MaterialTheme.spacing.xl))
                    }

                    is OperationState.Success -> {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(R.string.icon_desc_succeeded),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.graphicsLayer { alpha = iconAlpha },
                        )
                    }

                    is OperationState.Failed -> {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.icon_desc_failed),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.graphicsLayer { alpha = iconAlpha },
                        )
                    }

                    null -> {}
                }
            }
        },
    )
}
