package xyz.sevive.arcaeaoffline.ui.components.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.ui.components.IconRow


object DialogDismissTextButtonDefaults {
    val defaultColors: ButtonColors
        @Composable get() = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary,
        )
}

@Composable
fun DialogDismissTextButton(
    onClick: () -> Unit,
    customIcon: (@Composable () -> Unit)? = null,
    customLabel: (@Composable () -> Unit)? = null,
    colors: ButtonColors = DialogDismissTextButtonDefaults.defaultColors,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        colors = colors,
    ) {
        IconRow {
            customIcon?.invoke() ?: Icon(Icons.Default.Close, contentDescription = null)
            customLabel?.invoke() ?: Text(stringResource(android.R.string.cancel))
        }
    }
}
