package xyz.sevive.arcaeaoffline.ui.screens.settings.aboutlibraries

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsScreenDestination

@Composable
internal fun SettingsAboutlibrariesScreen(onNavigateUp: () -> Unit) {
    val libraries by produceLibraries(R.raw.aboutlibraries)

    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        title = stringResource(SettingsScreenDestination.Aboutlibraries.title),
    ) {
        LibrariesContainer(libraries, Modifier.fillMaxSize())
    }
}
