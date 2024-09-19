package xyz.sevive.arcaeaoffline.ui.screens.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R


@Composable
internal fun SettingsLicenseNoticeCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier) {
        Row(
            Modifier.padding(dimensionResource(R.dimen.card_padding)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))
        ) {
            Icon(Icons.Default.Balance, contentDescription = null)

            Column {
                Text(
                    stringResource(R.string.open_source_license_notice),
                    Modifier.padding(bottom = dimensionResource(R.dimen.icon_text_padding)),
                )
                Text(
                    stringResource(R.string.copyright_notice),
                    fontSize = MaterialTheme.typography.labelLarge.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
