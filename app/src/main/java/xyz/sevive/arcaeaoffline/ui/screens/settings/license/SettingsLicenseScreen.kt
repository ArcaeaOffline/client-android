package xyz.sevive.arcaeaoffline.ui.screens.settings.license

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsScreenDestination


private const val LICENSE_FILENAME = "gpl-3.0.md"

@Composable
internal fun SettingsLicenseScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val licenseText by produceState<String?>(initialValue = null) {
        launch(Dispatchers.IO) {
            // the [Material3RichText] below will block the UI thread for a moment,
            // so we're adding a explicit delay to ensure the loading indicator is visible,
            // informing user that there is some loading in the background.
            delay(500L)
            value = if (context.assets.list("")?.contains(LICENSE_FILENAME) == true)
                IOUtils.toString(context.assets.open(LICENSE_FILENAME))
            else "License file [${LICENSE_FILENAME}] not found! Check your assets."
        }
    }

    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        title = stringResource(SettingsScreenDestination.License.title)
    ) {
        LazyColumn(modifier.fillMaxSize()) {
            item {
                licenseText?.let {
                    Material3RichText { Markdown(it) }
                } ?: Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
