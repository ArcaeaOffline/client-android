package xyz.sevive.arcaeaoffline.ui.screens.settings.unstablealert

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.HorizontalRule
import com.halilibo.richtext.ui.material3.Material3RichText
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.navigation.SettingsScreenDestination


@Composable
internal fun SettingsUnstableAlertScreen(
    onNavigateUp: () -> Unit,
    vm: SettingsUnstableAlertScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val unstableAlertRead by vm.unstableAlertRead.collectAsStateWithLifecycle()
    LaunchedEffect(unstableAlertRead) {
        if (unstableAlertRead == false) vm.setUnstableAlertRead(value = true)
    }

    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        title = stringResource(SettingsScreenDestination.UnstableAlert.title),
        Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(all = dimensionResource(R.dimen.page_padding))
        ) {
            item {
                Material3RichText {
                    Markdown(stringResource(R.string.unstable_version_alert_screen_confirm_prompt_markdown))

                    HorizontalRule()

                    Markdown(stringResource(R.string.unstable_version_alert_screen_details_markdown))
                }
            }
        }
    }
}
