package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import android.annotation.SuppressLint
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.ListGroupHeader
import xyz.sevive.arcaeaoffline.ui.components.preferences.SliderPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.components.preferences.SwitchPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import kotlin.math.round


@Composable
private fun OcrQueuePreferencesDialogContent(
    uiState: OcrQueueScreenViewModel.PreferencesUiState,
    onSetCheckIsImage: (Boolean) -> Unit,
    onSetCheckIsArcaeaImage: (Boolean) -> Unit,
    onSetParallelCount: (Int) -> Unit,
) {
    val parallelCountValueRange = remember(uiState.parallelCountMin, uiState.parallelCountMax) {
        uiState.parallelCountMin.toFloat()..uiState.parallelCountMax.toFloat()
    }
    val parallelCountSliderSteps = remember(uiState.parallelCountMin, uiState.parallelCountMax) {
        (uiState.parallelCountMax - uiState.parallelCountMin) / 2 - 1
    }

    LazyColumn {
        item {
            ListGroupHeader(stringResource(R.string.ocr_queue_add_image_options_title))
        }

        item {
            SwitchPreferencesWidget(
                value = uiState.checkIsImage,
                onValueChange = { onSetCheckIsImage(it) },
                title = stringResource(R.string.ocr_queue_add_image_options_check_is_image),
            )
        }

        item {
            SwitchPreferencesWidget(
                value = uiState.checkIsArcaeaImage,
                onValueChange = { onSetCheckIsArcaeaImage(it) },
                title = stringResource(R.string.ocr_queue_add_image_options_detect_screenshot),
            )
        }

        item {
            ListGroupHeader(stringResource(R.string.ocr_queue_queue_options_title))
        }

        item {
            SliderPreferencesWidget(
                value = uiState.parallelCount.toFloat(),
                onValueChange = { onSetParallelCount(round(it).toInt()) },
                icon = Icons.AutoMirrored.Default.Sort,
                title = stringResource(R.string.ocr_queue_queue_options_parallel_count),
                description = uiState.parallelCount.toString(),
                valueRange = parallelCountValueRange,
                steps = parallelCountSliderSteps,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrQueuePreferencesDialog(
    onDismissRequest: () -> Unit,
    uiState: OcrQueueScreenViewModel.PreferencesUiState,
    onSetCheckIsImage: (Boolean) -> Unit,
    onSetCheckIsArcaeaImage: (Boolean) -> Unit,
    onSetParallelCount: (Int) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Card {
            OcrQueuePreferencesDialogContent(
                uiState = uiState,
                onSetCheckIsImage = onSetCheckIsImage,
                onSetCheckIsArcaeaImage = onSetCheckIsArcaeaImage,
                onSetParallelCount = onSetParallelCount,
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@PreviewLightDark
@Composable
private fun OcrQueuePreferencesDialogPreview() {
    var uiState by remember {
        mutableStateOf(
            OcrQueueScreenViewModel.PreferencesUiState(
                checkIsImage = true,
                checkIsArcaeaImage = true,
                parallelCount = 4,
                parallelCountMax = 32,
            )
        )
    }

    ArcaeaOfflineTheme {
        Card {
            Text(uiState.toString())

            OcrQueuePreferencesDialogContent(
                uiState = uiState,
                onSetCheckIsImage = { uiState = uiState.copy(checkIsImage = it) },
                onSetCheckIsArcaeaImage = { uiState = uiState.copy(checkIsArcaeaImage = it) },
                onSetParallelCount = { uiState = uiState.copy(parallelCount = it) },
            )
        }
    }
}
