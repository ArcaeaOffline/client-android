package xyz.sevive.arcaeaoffline.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R

/**
 * Simple wrapper of ListDetailPaneScaffold with a general page padding.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun <T> GeneralEntryScreen(
    navigator: ThreePaneScaffoldNavigator<T>,
    listPane: @Composable ThreePaneScaffoldScope.() -> Unit,
    detailPane: @Composable ThreePaneScaffoldScope.(T) -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.page_padding))
    ) {
        BackHandler(navigator.canNavigateBack()) {
            navigator.navigateBack()
        }

        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    listPane()
                }
            },
            detailPane = {
                AnimatedPane {
                    Crossfade(
                        targetState = navigator.currentDestination?.content,
                        modifier = Modifier.fillMaxSize(),
                        label = "GeneralEntryScreenDetailPaneWrapper",
                    ) {
                        if (it != null) {
                            detailPane(it)
                        } else {
                            Box(Modifier.fillMaxSize()) {
                                Icon(
                                    Icons.AutoMirrored.Default.MenuOpen,
                                    contentDescription = null,
                                    Modifier
                                        .size(75.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            },
        )
    }
}
