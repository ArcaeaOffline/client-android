package xyz.sevive.arcaeaoffline.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R

@Composable
fun SubScreenContainer(
    topBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = snackbarHost,
    ) {
        Box(Modifier.padding(it)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreenTopAppBar(
    onNavigateUp: () -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = { title() },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    stringResource(R.string.icon_desc_navigate_back)
                )
            }
        },
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreenContainer(
    onNavigateUp: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    SubScreenContainer(
        modifier = modifier,
        topBar = { SubScreenTopAppBar(onNavigateUp = onNavigateUp, title = { Text(title) }) },
        snackbarHost = snackbarHost,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreenContainer(
    onNavigateUp: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    SubScreenContainer(
        modifier = modifier,
        topBar = {
            SubScreenTopAppBar(
                onNavigateUp = onNavigateUp,
                title = { Text(title) },
                actions = actions,
            )
        },
        snackbarHost = snackbarHost,
        content = content,
    )
}
