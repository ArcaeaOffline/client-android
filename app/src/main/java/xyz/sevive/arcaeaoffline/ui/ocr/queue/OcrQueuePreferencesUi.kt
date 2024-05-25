package xyz.sevive.arcaeaoffline.ui.ocr.queue

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsSlider
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import kotlin.math.round

@Composable
internal fun SettingsTitle(text: String) {
    Text(
        text,
        Modifier.padding(vertical = dimensionResource(R.dimen.general_icon_text_padding)),
        style = MaterialTheme.typography.titleLarge,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrQueuePreferencesDialog(
    onDismissRequest: () -> Unit,
    viewModel: OcrQueueViewModel,
) {
    val coroutineScope = rememberCoroutineScope()

    val checkIsImage by viewModel.checkIsImage.collectAsState()
    val detectScreenshot by viewModel.checkIsArcaeaImage.collectAsState()
    val channelCapacity by viewModel.channelCapacity.collectAsState()
    val parallelCount by viewModel.parallelCount.collectAsState()

    val parallelCountMin = viewModel.parallelCountMin
    val parallelCountMax = viewModel.parallelCountMax

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Card {
            LazyColumn(
                contentPadding = PaddingValues(dimensionResource(R.dimen.general_page_padding)),
            ) {
                item {
                    SettingsTitle(stringResource(R.string.ocr_queue_add_image_options_title))
                }

                item {
                    SettingsCheckbox(
                        state = checkIsImage,
                        onCheckedChange = {
                            coroutineScope.launch { viewModel.setCheckIsImage(it) }
                        },
                        title = { Text(stringResource(R.string.ocr_queue_add_image_options_check_is_image)) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }

                item {
                    SettingsCheckbox(
                        state = detectScreenshot,
                        onCheckedChange = {
                            coroutineScope.launch { viewModel.setCheckIsArcaeaImage(it) }
                        },
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
                        title = { Text(stringResource(R.string.ocr_queue_queue_options_channel_capacity) + " - $channelCapacity") },
                        icon = { Icon(Icons.Default.Layers, contentDescription = null) },
                        value = channelCapacity.toFloat(),
                        onValueChange = {
                            coroutineScope.launch { viewModel.setChannelCapacity(round(it).toInt()) }
                        },
                        valueRange = 0f..50f,
                        steps = (50 - 0) / 5 - 1,
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }

                item {
                    SettingsSlider(
                        title = { Text(stringResource(R.string.ocr_queue_queue_options_parallel_count) + " - $parallelCount") },
                        icon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null) },
                        value = parallelCount.toFloat(),
                        onValueChange = {
                            coroutineScope.launch { viewModel.setParallelCount(round(it).toInt()) }
                        },
                        valueRange = parallelCountMin.toFloat()..parallelCountMax.toFloat(),
                        steps = (parallelCountMax - parallelCountMin) / 2 - 1,
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }
        }
    }
}
