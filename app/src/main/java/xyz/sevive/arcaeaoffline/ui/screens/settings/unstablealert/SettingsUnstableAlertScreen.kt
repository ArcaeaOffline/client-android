package xyz.sevive.arcaeaoffline.ui.screens.settings.unstablealert

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.compose.LazyMarkdownSuccess
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.rememberMarkdownState
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsSubScreen

@Composable
internal fun SettingsUnstableAlertScreen(vm: SettingsUnstableAlertScreenViewModel = koinViewModel()) {
    val unstableAlertRead by vm.unstableAlertRead.collectAsStateWithLifecycle()
    LaunchedEffect(unstableAlertRead) {
        if (unstableAlertRead == false) vm.setUnstableAlertRead(value = true)
    }

    val resources = LocalResources.current
    val unstableAlertContent =
        remember {
            buildString {
                append(resources.getString(R.string.unstable_version_alert_screen_confirm_prompt_markdown))
                append("\n\n---\n\n")
                append(resources.getString(R.string.unstable_version_alert_screen_details_markdown))
            }
        }
    val markdownState = rememberMarkdownState(unstableAlertContent)

    SubScreenContainer(
        title = stringResource(SettingsSubScreen.UnstableAlert.title),
        Modifier.fillMaxSize(),
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
