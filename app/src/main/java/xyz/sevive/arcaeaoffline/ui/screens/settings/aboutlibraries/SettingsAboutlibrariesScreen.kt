package xyz.sevive.arcaeaoffline.ui.screens.settings.aboutlibraries

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsScreenDestination


@Composable
internal fun SettingsAboutlibrariesScreen(
    onNavigateUp: () -> Unit,
) {
    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        title = stringResource(SettingsScreenDestination.Aboutlibraries.title)
    ) {
        LibrariesContainer(Modifier.fillMaxSize())
    }
}
