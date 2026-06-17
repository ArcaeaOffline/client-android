package xyz.sevive.arcaeaoffline.ui.screens.settings.license

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.mikepenz.markdown.compose.LazyMarkdownSuccess
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.rememberMarkdownState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsSubScreen

private const val LICENSE_FILENAME = "gpl-3.0.md"

@Composable
internal fun SettingsLicenseScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val licenseText by produceState<String?>(initialValue = null) {
        launch(Dispatchers.IO) {
            value =
                if (context.assets.list("")?.contains(LICENSE_FILENAME) == true) {
                    context.assets
                        .open(LICENSE_FILENAME)
                        .bufferedReader()
                        .use { it.readText() }
                } else {
                    "License file [${LICENSE_FILENAME}] not found! Check your assets."
                }
        }
    }
    val markdownState = rememberMarkdownState(licenseText ?: "")

    SubScreenContainer(
        title = stringResource(SettingsSubScreen.License.title),
        modifier = modifier,
    ) {
        Markdown(
            markdownState = markdownState,
            success = { state, components, modifier ->
                LazyMarkdownSuccess(
                    state,
                    components,
                    modifier,
                    contentPadding = PaddingValues(dimensionResource(R.dimen.page_padding)),
                )
            },
            loading = {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
