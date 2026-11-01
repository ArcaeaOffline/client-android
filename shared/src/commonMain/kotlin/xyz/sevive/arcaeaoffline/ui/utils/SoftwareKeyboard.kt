package xyz.sevive.arcaeaoffline.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager

/** Whether the software keyboard is currently shown. Always false where there is no software keyboard. */
@Composable
internal expect fun isSoftwareKeyboardVisible(): Boolean

/**
 * Clears focus when the software keyboard is dismissed, provided the keyboard was shown for the
 * current focus session.
 *
 * A field focused without the keyboard, for example through a hardware keyboard or programmatic
 * focus, keeps its focus.
 */
@Composable
fun Modifier.clearFocusOnKeyboardDismiss(): Modifier {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    if (isFocused) {
        // Scoped to the focus session: leaves composition when focus is lost.
        var keyboardShown by remember { mutableStateOf(false) }
        val softwareKeyboardVisible = isSoftwareKeyboardVisible()

        LaunchedEffect(softwareKeyboardVisible) {
            if (softwareKeyboardVisible) {
                keyboardShown = true
            } else if (keyboardShown) {
                focusManager.clearFocus()
            }
        }
    }

    return onFocusChanged { isFocused = it.isFocused }
}
