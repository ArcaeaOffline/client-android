package xyz.sevive.arcaeaoffline.ui.ocr.queue

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsSlider
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import kotlin.math.round

@Composable
internal fun SettingsTitle(text: String) {
    Text(
        text,
        Modifier.padding(vertical = dimensionResource(R.dimen.icon_text_padding)),
        style = MaterialTheme.typography.titleLarge,
    )
}

@Composable
private fun OcrQueuePreferencesDialogContent(
    uiState: OcrQueueViewModel.PreferencesUiState,
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

    LazyColumn(
        contentPadding = PaddingValues(dimensionResource(R.dimen.page_padding)),
    ) {
        item {
            SettingsTitle(stringResource(R.string.ocr_queue_add_image_options_title))
        }

        item {
            SettingsCheckbox(
                state = uiState.checkIsImage,
                onCheckedChange = { onSetCheckIsImage(it) },
                title = { Text(stringResource(R.string.ocr_queue_add_image_options_check_is_image)) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }

        item {
            SettingsCheckbox(
                state = uiState.checkIsArcaeaImage,
                onCheckedChange = { onSetCheckIsArcaeaImage(it) },
                title = { Text(stringResource(R.string.ocr_queue_add_image_options_detect_screenshot)) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }

        item {
            Text(
                stringResource(R.string.ocr_queue_queue_options_title),
                style = MaterialTheme.typography.titleLarge,
            )
        }

        item {
            SettingsSlider(
                title = { Text(stringResource(R.string.ocr_queue_queue_options_parallel_count) + " - ${uiState.parallelCount}") },
                icon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) },
                value = uiState.parallelCount.toFloat(),
                onValueChange = { onSetParallelCount(round(it).toInt()) },
                valueRange = parallelCountValueRange,
                steps = parallelCountSliderSteps,
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrQueuePreferencesDialog(
    onDismissRequest: () -> Unit,
    uiState: OcrQueueViewModel.PreferencesUiState,
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
            OcrQueueViewModel.PreferencesUiState(
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
