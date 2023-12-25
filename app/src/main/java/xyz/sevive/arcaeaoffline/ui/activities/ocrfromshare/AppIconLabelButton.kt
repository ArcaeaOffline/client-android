package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R


@Composable
internal fun AppIconLabelButton(
    onClick: () -> Unit,
    appIcon: @Composable () -> Unit,
    appLabel: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actionText: (@Composable () -> Unit)? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    trailingIcon: @Composable () -> Unit = { Icon(Icons.AutoMirrored.Default.ArrowForward, null) },
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.action_button_icon_text_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            appIcon()

            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.general_icon_text_padding)),
            ) {
                appLabel()

                if (actionText != null) {
                    actionText()
                }
            }

            trailingIcon()
        }
    }
}
