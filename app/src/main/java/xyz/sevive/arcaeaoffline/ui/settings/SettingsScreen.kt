package xyz.sevive.arcaeaoffline.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.constants.IS_UNSTABLE_VERSION
import xyz.sevive.arcaeaoffline.ui.components.IconRow

@Composable
internal fun settingsTitleActionCardShape(): CornerBasedShape {
    return RoundedCornerShape(
        topStart = MaterialTheme.shapes.medium.topStart,
        topEnd = MaterialTheme.shapes.medium.topEnd,
        bottomStart = CornerSize(0.dp),
        bottomEnd = CornerSize(0.dp),
    )
}

@Composable
fun UnstableBuildAlert(modifier: Modifier = Modifier, showDetails: Boolean = true) {
    Card(
        modifier,
        shape = CutCornerShape(
            0.dp,
            dimensionResource(R.dimen.action_card_padding),
            0.dp,
            dimensionResource(R.dimen.action_card_padding),
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error,
        ),
    ) {
        Column(Modifier.padding(dimensionResource(R.dimen.action_card_padding))) {
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

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    Box(
        modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.general_page_padding))
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))) {
            if (IS_UNSTABLE_VERSION) {
                item {
                    UnstableBuildAlert(Modifier.fillMaxWidth(), showDetails = true)
                }
            }

            item {
                SettingsOcrDependencies(settingsViewModel)
            }

            item {
                SettingsAbout()
            }
        }
    }
}
