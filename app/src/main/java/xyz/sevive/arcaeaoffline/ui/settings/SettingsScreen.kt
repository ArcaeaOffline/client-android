package xyz.sevive.arcaeaoffline.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.data.IS_UNSTABLE_VERSION

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
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(),
) {
    Box(
        modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.page_padding))
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))) {
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
