package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.enqueuechecker

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import kotlin.math.roundToInt


@Composable
internal fun OcrQueueEnqueueCheckerFloatingActionButton(
    ocrQueueRunning: Boolean,
    modifier: Modifier = Modifier,
    vm: OcrQueueEnqueueCheckerViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current

    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        // persist access permission to these images, ensuring the image preview function
        // will work even if the application restarted
        val permissionFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        uris.forEach { context.contentResolver.takePersistableUriPermission(it, permissionFlags) }

        vm.addImageFiles(uris)
    }

    val pickFolderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // persistent access permission to this folder
            val permissionFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, permissionFlags)

            val folder = DocumentFile.fromTreeUri(context, uri)
            folder?.let { vm.addFolder(it) }
        }
    }

    OcrQueueEnqueueCheckerFloatingActionButton(
        uiState = uiState,
        onPickImages = { pickImagesLauncher.launch("image/*") },
        onPickFolder = { pickFolderLauncher.launch(null) },
        onStopJob = { vm.cancelWork() },
        ocrQueueRunning = ocrQueueRunning,
        modifier = modifier,
    )
}

@Composable
internal fun OcrQueueEnqueueCheckerFloatingActionButton(
    onPickImages: () -> Unit,
    onPickFolder: () -> Unit,
    onStopJob: () -> Unit,
    ocrQueueRunning: Boolean,
    uiState: OcrQueueEnqueueCheckerViewModel.UiState,
    modifier: Modifier = Modifier,
) {
    val enabled = remember(ocrQueueRunning) { !ocrQueueRunning }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = enabled) {
        if (!enabled) showBottomSheet = false
    }

    if (showBottomSheet) {
        OcrQueueEnqueueCheckerBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            onPickImagesRequest = {
                onPickImages()
                showBottomSheet = false
            },
            onPickFolderRequest = {
                onPickFolder()
                showBottomSheet = false
            },
            onStopJobRequest = if (uiState.isRunning) {
                {
                    onStopJob()
                    showBottomSheet = false
                }
            } else null,
        )
    }

    val progress = remember(uiState.progress) {
        uiState.progress?.let { it.first.toFloat() / it.second }
    }
    val progressText = remember(progress) { progress?.let { "${(it * 100).roundToInt()}%" } }
    val imagePlusIconAlpha by animateFloatAsState(
        targetValue = if (uiState.isPreparing || progress != null) 0.05f else 1f,
        label = "imagePlusIconAlpha",
    )

    AnimatedVisibility(
        visible = enabled,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        FloatingActionButton(onClick = { if (enabled) showBottomSheet = true }) {
            Box {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_image_plus),
                    contentDescription = null,
                    Modifier
                        .align(Alignment.Center)
                        .alpha(imagePlusIconAlpha)
                )

                if (uiState.isPreparing) {
                    CircularProgressIndicator(
                        Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                    )
                }

                progress?.let {
                    CircularProgressIndicator(
                        progress = { it },
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                    )

                    Text(
                        progressText ?: "",
                        Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Light,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OcrQueueEnqueueCheckerFloatingActionButtonPreview() {
    ArcaeaOfflineTheme {
        Surface {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OcrQueueEnqueueCheckerFloatingActionButton(
                    onPickImages = {},
                    onPickFolder = {},
                    onStopJob = {},
                    uiState = OcrQueueEnqueueCheckerViewModel.UiState(),
                    ocrQueueRunning = false,
                )

                OcrQueueEnqueueCheckerFloatingActionButton(
                    onPickImages = {},
                    onPickFolder = {},
                    onStopJob = {},
                    uiState = OcrQueueEnqueueCheckerViewModel.UiState(
                        isPreparing = true,
                    ),
                    ocrQueueRunning = false,
                )

                OcrQueueEnqueueCheckerFloatingActionButton(
                    onPickImages = {},
                    onPickFolder = {},
                    onStopJob = {},
                    uiState = OcrQueueEnqueueCheckerViewModel.UiState(
                        isRunning = true,
                        progress = 33 to 100,
                    ),
                    ocrQueueRunning = false,
                )
            }
        }
    }
}
