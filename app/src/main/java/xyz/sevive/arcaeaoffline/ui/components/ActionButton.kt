package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DisabledByDefault
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

@Composable
fun ActionButton(
    onClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.shape,
    buttonColors: ButtonColors? = null,
    headSlot: @Composable () -> Unit = {},
    tailSlot: @Composable () -> Unit = {}
) {
    val appliedColors = buttonColors ?: ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer)
    )

    Button(
        onClick,
        modifier,
        enabled = enabled,
        shape = shape,
        colors = appliedColors,
        contentPadding = PaddingValues(dimensionResource(R.dimen.action_button_padding)),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            headSlot()

            Spacer(modifier.width(dimensionResource(R.dimen.action_button_icon_text_padding)))

            Column {
                Text(title, style = MaterialTheme.typography.titleLarge)
                if (description != null) {
                    Text(description, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(modifier.width(dimensionResource(R.dimen.action_button_icon_text_padding)))

            tailSlot()

            Spacer(modifier.width(dimensionResource(R.dimen.action_button_icon_text_padding)))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActionButtonPreview() {
    ArcaeaOfflineTheme {
        Column(
            Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton({}, "Test")
            ActionButton({}, "Test w/ desc", description = "wow description")
            ActionButton(
                onClick = {},
                title = "Test w/ icon",
                description = "wow an arrow",
                tailSlot = {
                    Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                })
            ActionButton(onClick = {},
                title = "Test w/ two icons",
                description = "wow icons",
                headSlot = {
                    Icon(Icons.Default.UploadFile, null)
                },
                tailSlot = {
                    Icon(Icons.Default.ChevronRight, null)
                })
            ActionButton(onClick = {},
                title = "Test disabled",
                description = "wow disabled",
                enabled = false,
                headSlot = {
                    Icon(Icons.Default.Cancel, null)
                },
                tailSlot = {
                    Icon(Icons.Default.DisabledByDefault, null)
                })
        }
    }
}
