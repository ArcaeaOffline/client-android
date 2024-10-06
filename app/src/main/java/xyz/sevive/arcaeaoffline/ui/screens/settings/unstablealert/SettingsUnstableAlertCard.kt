package xyz.sevive.arcaeaoffline.ui.screens.settings.unstablealert

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.IconRow


@Composable
private fun UnstableBuildAlertCardContent(showDetails: Boolean = true) {
    Column(Modifier.padding(dimensionResource(R.dimen.action_button_padding))) {
        IconRow {
            Icon(painterResource(R.drawable.ic_unstable_build), null)
            Text(
                stringResource(R.string.unstable_version_alert_title),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        if (showDetails) {
            Text(
                stringResource(R.string.unstable_version_alert_short_description),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun UnstableBuildAlertCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showDetails: Boolean = true,
    shape: Shape = CutCornerShape(
        topStart = 0.dp,
        topEnd = dimensionResource(R.dimen.action_button_padding),
        bottomEnd = 0.dp,
        bottomStart = dimensionResource(R.dimen.action_button_padding),
    ),
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.error,
    ),
) {
    if (onClick != null) {
        Card(onClick, modifier, shape = shape, colors = colors) {
            UnstableBuildAlertCardContent(showDetails = showDetails)
        }
    } else {
        Card(modifier, shape = shape, colors = colors) {
            UnstableBuildAlertCardContent(showDetails = showDetails)
        }
    }
}
