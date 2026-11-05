package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.PainterState
import com.github.panpf.sketch.rememberAsyncImageState
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.MainActivity
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha
import xyz.sevive.arcaeaoffline.permissions.storage.SaveImageToGallery
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareViewModel.OcrResultState
import xyz.sevive.arcaeaoffline.ui.common.PermissionRequiredDialog
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.ImagePreviewDialogFullscreen
import xyz.sevive.arcaeaoffline.ui.components.TextItem
import xyz.sevive.arcaeaoffline.ui.navigation.OcrSubScreen
import xyz.sevive.arcaeaoffline.ui.theme.spacing

@Composable
private fun OcrFromShareReturnToShareAppButton(
    shareSource: OcrFromShareViewModel.ShareSourceUiState,
    onReturnToShareApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconSize = Icons.Default.Apps.defaultHeight

    TextItem(
        onClick = onReturnToShareApp,
        title = shareSource.name ?: stringResource(R.string.ocr_from_share_return_to_share_source),
        content = if (shareSource.name != null) stringResource(R.string.ocr_from_share_return_to_share_source) else null,
        leadingSlot = {
            shareSource.icon?.let {
                Image(it, null, Modifier.size(iconSize))
            } ?: Icon(Icons.Default.Apps, null, Modifier.size(iconSize))
        },
        trailingSlot = {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        },
        modifier = modifier,
    )
}

@Composable
private fun OcrFromShareStayInAppButton(
    onStayInApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextItem(
        onClick = onStayInApp,
        title =
            String.format(
                stringResource(R.string.ocr_from_share_stay_in_app),
                stringResource(R.string.app_name),
            ),
        leadingSlot = { Icon(Icons.Default.Home, null) },
        trailingSlot = {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        },
        modifier = modifier,
    )
}

@Composable
private fun OcrFromShareScreenExpanded(
    sharedImageUri: Uri?,
    imageSaved: Boolean,
    onSaveImage: () -> Unit,
    dependencyUiState: OcrFromShareViewModel.OcrDependencyViewersUiState?,
    shareSource: OcrFromShareViewModel.ShareSourceUiState,
    onReturnToShareApp: () -> Unit,
    onStayInApp: () -> Unit,
    onNavigateToDependencies: () -> Unit,
    snackbarHost: @Composable () -> Unit,
    resultSection: @Composable ColumnScope.() -> Unit,
    actionsSection: @Composable ColumnScope.() -> Unit,
) {
    // Top bar removed since it takes a certain amount of height but contains no useful information
    // Especially useless on landscape phones
    Scaffold(snackbarHost = snackbarHost) { innerPadding ->
        Row(
            Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .padding(innerPadding)
                .padding(MaterialTheme.spacing.pagePadding),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.pagePadding),
        ) {
            OcrFromShareImage(
                sharedImageUri,
                Modifier.weight(1f),
                contentScale = ContentScale.Crop,
                // Bias the crop window left for max recall to be visible
                // Right side contains nearly useless partner image for OCR data correction
                alignment = BiasAlignment(-0.42f, 0f),
                imageSaved = imageSaved,
                onSaveImage = onSaveImage,
            )

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                OcrFromShareOcrDependencyStatusCard(dependencyUiState, onNavigateToDependencies)

                resultSection()

                actionsSection()

                OcrFromShareReturnToShareAppButton(shareSource, onReturnToShareApp)

                OcrFromShareStayInAppButton(onStayInApp)
            }
        }
    }
}

@Composable
private fun OcrFromShareScreenCompact(
    sharedImageUri: Uri?,
    imageSaved: Boolean,
    onSaveImage: () -> Unit,
    dependencyUiState: OcrFromShareViewModel.OcrDependencyViewersUiState?,
    shareSource: OcrFromShareViewModel.ShareSourceUiState,
    onReturnToShareApp: () -> Unit,
    onStayInApp: () -> Unit,
    onNavigateToDependencies: () -> Unit,
    snackbarHost: @Composable () -> Unit,
    resultSection: @Composable ColumnScope.() -> Unit,
    actionsSection: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        topBar = { OcrFromShareTopBar() },
        snackbarHost = snackbarHost,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = innerPadding.calculateTopPadding() + MaterialTheme.spacing.pagePadding,
                        bottom = innerPadding.calculateBottomPadding() + MaterialTheme.spacing.pagePadding,
                    ),
        ) {
            Column(
                Modifier.padding(
                    start = MaterialTheme.spacing.pagePadding,
                    end = MaterialTheme.spacing.pagePadding,
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                OcrFromShareOcrDependencyStatusCard(dependencyUiState, onNavigateToDependencies)

                OcrFromShareImage(
                    sharedImageUri,
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    imageSaved = imageSaved,
                    onSaveImage = onSaveImage,
                )

                resultSection()

                actionsSection()
            }

            OcrFromShareReturnToShareAppButton(shareSource, onReturnToShareApp)

            OcrFromShareStayInAppButton(onStayInApp)
        }
    }
}

@Composable
fun OcrFromShareScreen(
    onReturnToShareApp: () -> Unit,
    onStayInApp: () -> Unit,
    viewModel: OcrFromShareViewModel,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val navigateToDependencies = {
        // User should expect returning to this activity after setting up,
        // so do not finish activity here.
        // CLEAR_TOP tells the running instance to recreate and re-read the
        // route extra for jumping.
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_INITIAL_ROUTE, OcrSubScreen.Dependencies.route)
            }
        context.startActivity(intent)
    }

    val sharedImageUri by viewModel.sharedImageUri.collectAsStateWithLifecycle()
    val dependencyUiState by viewModel.ocrDependencyViewersUiState.collectAsStateWithLifecycle()
    val shareSource by viewModel.shareSourceUiState.collectAsStateWithLifecycle()
    val resultState by viewModel.ocrResultState.collectAsStateWithLifecycle()
    val playResultSaveState by viewModel.playResultSaveState.collectAsStateWithLifecycle()
    val imageSaved by viewModel.imageSaved.collectAsStateWithLifecycle()
    val warnings by viewModel.warnings.collectAsStateWithLifecycle()
    val display by viewModel.display.collectAsStateWithLifecycle()

    val resultSection: @Composable ColumnScope.() -> Unit =
        remember {
            movableContentOf<ColumnScope> {
                OcrFromShareOcrResult(
                    playResult = (resultState as? OcrResultState.Done)?.playResult,
                    onSetPlayResult = { viewModel.setPlayResult(it) },
                    display = display,
                    warnings = warnings,
                    playResultSaveState = playResultSaveState,
                    exception = (resultState as? OcrResultState.Failed)?.exception,
                    onRetryOcr = { viewModel.retryOcr() },
                )
            }
        }

    val handleSaveImage = { viewModel.saveImage() }
    val requestWriteStoragePermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { if (it) handleSaveImage() }

    var showSavePicturePermissionRequiredDialog by rememberSaveable { mutableStateOf(false) }
    if (showSavePicturePermissionRequiredDialog) {
        PermissionRequiredDialog(
            onDismiss = { showSavePicturePermissionRequiredDialog = false },
            onConfirm = {
                requestWriteStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                showSavePicturePermissionRequiredDialog = false
            },
            functionName = stringResource(R.string.permission_function_save_image),
            permissions = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        )
    }

    val onSaveImage = {
        if (SaveImageToGallery.checkPermission(context)) {
            handleSaveImage()
        } else {
            showSavePicturePermissionRequiredDialog = true
        }
    }

    val actionsSection: @Composable ColumnScope.() -> Unit =
        remember {
            movableContentOf<ColumnScope> {
                OcrFromShareActions(
                    hasPlayResult = resultState is OcrResultState.Done,
                    playResultSaveState = playResultSaveState,
                    warnings = warnings,
                    onSavePlayResult = { coroutineScope.launch { viewModel.savePlayResult() } },
                    onCachePlayResult = {
                        // TODO: coroutineScope.launch { viewModel.cachePlayResult(context) }
                        Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
                    },
                )
            }
        }

    // Reload on every resume to possibly reflect the latest status
    // e.g. returning from the app's dependency setup
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.reloadOcrDependencyViewersUiState()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                snackbarHostState.showSnackbar(event.asString(context.resources), withDismissAction = true)
            }
        }
    }

    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (landscape) {
        OcrFromShareScreenExpanded(
            sharedImageUri = sharedImageUri,
            imageSaved = imageSaved,
            onSaveImage = onSaveImage,
            dependencyUiState = dependencyUiState,
            shareSource = shareSource,
            onReturnToShareApp = onReturnToShareApp,
            onStayInApp = onStayInApp,
            onNavigateToDependencies = navigateToDependencies,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            resultSection = resultSection,
            actionsSection = actionsSection,
        )
    } else {
        OcrFromShareScreenCompact(
            sharedImageUri = sharedImageUri,
            imageSaved = imageSaved,
            onSaveImage = onSaveImage,
            dependencyUiState = dependencyUiState,
            shareSource = shareSource,
            onReturnToShareApp = onReturnToShareApp,
            onStayInApp = onStayInApp,
            onNavigateToDependencies = navigateToDependencies,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            resultSection = resultSection,
            actionsSection = actionsSection,
        )
    }
}

@Composable
private fun OcrFromShareImage(
    uri: Uri?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    imageSaved: Boolean,
    onSaveImage: () -> Unit,
) {
    var showImagePreview by rememberSaveable { mutableStateOf(false) }
    val imageState = rememberAsyncImageState()

    if (showImagePreview) {
        uri?.let {
            ImagePreviewDialogFullscreen(
                onDismissRequest = { showImagePreview = false },
                uri = it,
            )
        }
    }

    // Image tap and the fullscreen button share an interaction source
    // The press ripple draws on the button rather than the whole image
    val imageInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier.clickable(
            interactionSource = imageInteractionSource,
            indication = null,
            enabled = uri != null,
        ) { showImagePreview = true },
    ) {
        if (uri == null) {
            // placeholder box to keep layout stable
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Image,
                    contentDescription = null,
                    Modifier
                        .size(48.dp)
                        .secondaryItemAlpha(),
                )
            }
        } else {
            if (imageState.painterState == null || imageState.painterState is PainterState.Loading) {
                ImageSkeleton(Modifier.fillMaxSize())
            }

            AsyncImage(
                uri = uri.toString(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                state = imageState,
                contentScale = contentScale,
                alignment = alignment,
            )

            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(MaterialTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs),
            ) {
                FilledTonalButton(
                    onClick = { showImagePreview = true },
                    interactionSource = imageInteractionSource,
                ) {
                    IconRow {
                        Icon(Icons.Default.Fullscreen, null)
                        Text(stringResource(R.string.ocr_from_share_fullscreen))
                    }
                }

                FilledTonalButton(
                    onClick = onSaveImage,
                    enabled = !imageSaved,
                    // Disabled colors are explicit since the defaults are alpha-faded and the
                    // image would show through the button
                    colors =
                        ButtonDefaults.filledTonalButtonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                ) {
                    AnimatedContent(imageSaved) { saved ->
                        if (saved) {
                            IconRow {
                                Icon(Icons.Default.Check, null)
                                Text(stringResource(R.string.ocr_from_share_image_saved))
                            }
                        } else {
                            IconRow {
                                Icon(Icons.Default.FileDownload, null)
                                Text(stringResource(R.string.ocr_from_share_save_image_button))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageSkeleton(modifier: Modifier = Modifier) {
    val alpha by
        rememberInfiniteTransition(label = "ImageSkeleton")
            .animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "ImageSkeletonAlpha",
            )

    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = alpha)))
}
