package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import kotlin.math.roundToInt


@Composable
internal fun OcrQueueAddTaskFloatingActionButton(
    onPickImages: () -> Unit,
    onPickFolder: () -> Unit,
    onStopJob: () -> Unit,
    enabled: Boolean,
    uiState: OcrQueueScreenViewModel.EnqueueCheckerJobUiState,
    modifier: Modifier = Modifier,
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = enabled) {
        if (!enabled) showBottomSheet = false
    }

    if (showBottomSheet) {
        OcrQueueAddTaskBottomSheet(
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
    val iconAlpha by animateFloatAsState(
        targetValue = if (uiState.isPreparing || progress != null) 0.1f else 1f,
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
                        .alpha(iconAlpha)
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
private fun OcrQueueAddTaskFloatingActionButtonPreview() {
    ArcaeaOfflineTheme {
        Surface {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OcrQueueAddTaskFloatingActionButton(
                    onPickImages = {},
                    onPickFolder = {},
                    onStopJob = {},
                    uiState = OcrQueueScreenViewModel.EnqueueCheckerJobUiState(),
                    enabled = true,
                )

                OcrQueueAddTaskFloatingActionButton(
                    onPickImages = {},
                    onPickFolder = {},
                    onStopJob = {},
                    uiState = OcrQueueScreenViewModel.EnqueueCheckerJobUiState(
                        isPreparing = true,
                    ),
                    enabled = true,
                )

                OcrQueueAddTaskFloatingActionButton(
                    onPickImages = {},
                    onPickFolder = {},
                    onStopJob = {},
                    uiState = OcrQueueScreenViewModel.EnqueueCheckerJobUiState(
                        isRunning = true,
                        progress = 33 to 100,
                    ),
                    enabled = true,
                )
            }
        }
    }
}
