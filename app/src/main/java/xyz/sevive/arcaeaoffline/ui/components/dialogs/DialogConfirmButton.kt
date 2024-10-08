package xyz.sevive.arcaeaoffline.ui.components.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.ui.components.IconRow


object DialogConfirmButtonDefaults {
    val defaultColors: ButtonColors
        @Composable get() = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        )

    val dangerColors: ButtonColors
        @Composable get() = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error,
        )
}

@Composable
fun DialogConfirmButton(
    onClick: () -> Unit,
    customIcon: (@Composable () -> Unit)? = null,
    customLabel: (@Composable () -> Unit)? = null,
    colors: ButtonColors = DialogConfirmButtonDefaults.defaultColors,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = colors,
    ) {
        IconRow {
            customIcon?.invoke() ?: Icon(Icons.Default.Check, contentDescription = null)
            customLabel?.invoke() ?: Text(stringResource(android.R.string.ok))
        }
    }
}
