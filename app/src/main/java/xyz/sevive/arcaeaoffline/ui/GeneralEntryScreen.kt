package xyz.sevive.arcaeaoffline.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.round

/**
 * Simple wrapper of ListDetailPaneScaffold.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun <T> GeneralEntryScreen(
    navigator: ThreePaneScaffoldNavigator<T>,
    listPane: @Composable ThreePaneScaffoldScope.() -> Unit,
    detailPane: @Composable ThreePaneScaffoldScope.(T) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        BackHandler(navigator.canNavigateBack()) {
            coroutineScope.launch { navigator.navigateBack() }
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
                AnimatedPane(Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = navigator.currentDestination?.contentKey,
                        transitionSpec = {
                            (slideInVertically {
                                round(EaseOutCubic.transform(it * 0.025f)).toInt()
                            } + fadeIn(
                                animationSpec = tween(easing = EaseOutCubic)
                            )).togetherWith((slideOutVertically {
                                round(EaseInCubic.transform(it * 0.025f)).toInt()
                            } + fadeOut(
                                animationSpec = tween(easing = EaseInCubic)
                            )))
                        },
                        modifier = Modifier.fillMaxSize(),
                        label = "detailPaneTransition",
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
