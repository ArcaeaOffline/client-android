package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.theme.spacing

@Composable
internal fun OcrFromShareReturnToShareAppButton(
    onReturnToShareApp: () -> Unit,
    ocrFromShareViewModel: OcrFromShareViewModel,
    modifier: Modifier = Modifier,
) {
    val appName by ocrFromShareViewModel.shareSourceAppName.collectAsStateWithLifecycle()
    val appIcon by ocrFromShareViewModel.shareSourceAppIcon.collectAsStateWithLifecycle()

    val iconSize = Icons.Default.Apps.defaultHeight

    AppIconLabelButton(
        onClick = { onReturnToShareApp() },
        appIcon = {
            if (appIcon != null) {
                Image(appIcon!!, null, Modifier.size(iconSize))
            } else {
                Icon(Icons.Default.Apps, null, Modifier.size(iconSize))
            }
        },
        appLabel = {
            if (appName != null) {
                Text(appName!!)
            }
        },
        actionText = { Text(stringResource(R.string.ocr_from_share_return_to_share_source)) },
        modifier = modifier,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ),
    )
}

@Composable
internal fun OcrFromShareStayInAppButton(
    onStayInApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppIconLabelButton(
        onClick = { onStayInApp() },
        appIcon = {
            Icon(Icons.Default.Dashboard, null)
        },
        appLabel = {
            Text(
                String.format(
                    stringResource(R.string.ocr_from_share_stay_in_app),
                    stringResource(R.string.app_name),
                ),
            )
        },
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        modifier = modifier,
    )
}

@Composable
internal fun OcrFromShareScreenContentMedium(
    onReturnToShareApp: () -> Unit,
    onStayInApp: () -> Unit,
    viewModel: OcrFromShareViewModel,
) {
    val context = LocalContext.current

    val imageBitmap by viewModel.imageBitmap.collectAsStateWithLifecycle()
    val ocrDependencyViewersUiState by viewModel.ocrDependencyViewersUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.reloadOcrDependencyViewersUiState(context) }

    Scaffold(topBar = { OcrFromShareTopBar() }) { innerPadding ->
        Row(
            Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .padding(innerPadding)
                .padding(MaterialTheme.spacing.pagePadding),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.pagePadding),
        ) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                OcrFromShareOcrDependencyStatusCard(ocrDependencyViewersUiState)

                if (imageBitmap != null) {
                    Image(imageBitmap!!, null)
                }
            }

            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                item {
                    OcrFromShareOcrResult(viewModel)
                }

                item {
                    OcrFromShareActions(viewModel)
                }

                item {
                    OcrFromShareReturnToShareAppButton(onReturnToShareApp, viewModel)
                }

                item {
                    OcrFromShareStayInAppButton(onStayInApp)
                }
            }
        }
    }
}

@Composable
fun OcrFromShareScreenCompact(
    onReturnToShareApp: () -> Unit,
    onStayInApp: () -> Unit,
    viewModel: OcrFromShareViewModel,
) {
    val context = LocalContext.current

    val imageBitmap by viewModel.imageBitmap.collectAsStateWithLifecycle()
    val ocrDependencyViewersUiState by viewModel.ocrDependencyViewersUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.reloadOcrDependencyViewersUiState(context) }

    Scaffold(topBar = { OcrFromShareTopBar() }) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding),
            contentPadding =
                PaddingValues(
                    top = innerPadding.calculateTopPadding() + MaterialTheme.spacing.pagePadding,
                    bottom = innerPadding.calculateBottomPadding() + MaterialTheme.spacing.pagePadding,
                    start = MaterialTheme.spacing.pagePadding,
                    end = MaterialTheme.spacing.pagePadding,
                ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            item {
                OcrFromShareOcrDependencyStatusCard(ocrDependencyViewersUiState)
            }

            if (imageBitmap != null) {
                item {
                    Image(imageBitmap!!, null)
                }
            }

            item {
                OcrFromShareOcrResult(viewModel)
            }

            item {
                OcrFromShareActions(viewModel)
            }

            item {
                OcrFromShareReturnToShareAppButton(onReturnToShareApp, viewModel)
            }

            item {
                OcrFromShareStayInAppButton(onStayInApp)
            }
        }
    }
}

@Composable
fun OcrFromShareScreen(
    windowSizeClass: WindowSizeClass,
    onReturnToShareApp: () -> Unit,
    onStayInApp: () -> Unit,
    viewModel: OcrFromShareViewModel,
) {
    if (windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium) {
        OcrFromShareScreenContentMedium(
            onReturnToShareApp = onReturnToShareApp,
            onStayInApp = onStayInApp,
            viewModel = viewModel,
        )
    } else {
        OcrFromShareScreenCompact(
            onReturnToShareApp = onReturnToShareApp,
            onStayInApp = onStayInApp,
            viewModel = viewModel,
        )
    }
}
