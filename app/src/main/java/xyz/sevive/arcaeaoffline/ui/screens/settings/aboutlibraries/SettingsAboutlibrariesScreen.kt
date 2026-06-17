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
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsSubScreen

@Composable
internal fun SettingsAboutlibrariesScreen() {
    val libraries by produceLibraries(R.raw.aboutlibraries)

    SubScreenContainer(
        title = stringResource(SettingsSubScreen.Aboutlibraries.title),
    ) {
        LibrariesContainer(libraries, Modifier.fillMaxSize())
    }
}
