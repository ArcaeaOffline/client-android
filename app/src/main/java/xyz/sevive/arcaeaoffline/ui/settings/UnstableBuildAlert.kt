package xyz.sevive.arcaeaoffline.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.IconRow


@Composable
fun UnstableBuildAlert(modifier: Modifier = Modifier, showDetails: Boolean = true) {
    Card(
        modifier,
        shape = CutCornerShape(
            0.dp,
            dimensionResource(R.dimen.action_button_padding),
            0.dp,
            dimensionResource(R.dimen.action_button_padding),
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error,
        ),
    ) {
        Column(Modifier.padding(dimensionResource(R.dimen.action_button_padding))) {
            IconRow(icon = { Icon(painterResource(R.drawable.ic_unstable_build), null) }) {
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
}
