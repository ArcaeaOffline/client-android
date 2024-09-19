package xyz.sevive.arcaeaoffline.ui.screens.settings.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import xyz.sevive.arcaeaoffline.BuildConfig
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsScreenDestination
import xyz.sevive.arcaeaoffline.ui.screens.NavEntryNavigateButton


class AppIconBeingClickedTooManyTimesSoEmbarrassingException : Exception() {
    override val message: String = "(⁄ ⁄•⁄ω⁄•⁄ ⁄)"
}

@Composable
private fun AppIcon(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appIcon = remember {
        context.packageManager.getApplicationIcon(BuildConfig.APPLICATION_ID).toBitmap()
            .asImageBitmap()
    }

    Image(appIcon, contentDescription = null, modifier = modifier)
}

@Composable
internal fun SettingsAboutScreen(
    onNavigateUp: () -> Unit,
    onNavigateToLicenseScreen: () -> Unit,
    onNavigateToAboutlibrariesScreen: () -> Unit,
) {
    val appIconClickCrasherState = rememberAppIconClickCrasherState()

    val versionText = remember { "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})" }
    val appName = stringResource(R.string.app_name)
    val appNameText = remember(appIconClickCrasherState.count) {
        val count = appIconClickCrasherState.count
        if (count <= 4) "$appName (${appIconClickCrasherState.count})"
        else appName
    }

    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        title = stringResource(SettingsScreenDestination.About.title)
    ) {
        LazyColumn {
            item {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AppIcon(
                        Modifier
                            .clickable { appIconClickCrasherState.clicked() }
                            .size(100.dp)
                    )

                    Text(appNameText, style = MaterialTheme.typography.titleLarge)
                    Text(versionText, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    SettingsAboutCommunities()
                }
            }

            item {
                HorizontalDivider()
            }

            item {
                NavEntryNavigateButton(
                    titleResId = R.string.open_source_license_notice,
                    descriptionResId = R.string.copyright_notice,
                    icon = Icons.Default.Balance,
                ) {
                    onNavigateToLicenseScreen()
                }
            }

            item {
                NavEntryNavigateButton(
                    titleResId = SettingsScreenDestination.Aboutlibraries.title,
                    icon = Icons.Default.DataObject,
                ) {
                    onNavigateToAboutlibrariesScreen()
                }
            }
        }
    }
}
