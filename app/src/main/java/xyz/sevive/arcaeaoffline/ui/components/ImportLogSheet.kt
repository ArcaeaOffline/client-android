package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.data.imports.ImportLogEvent
import xyz.sevive.arcaeaoffline.data.imports.ImportLogObject
import xyz.sevive.arcaeaoffline.helpers.context.findActivity
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDate
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDateTime
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedTime
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.spacing
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Composable
private fun ImportLogObjectUi(
    log: ImportLogObject,
    widthSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current

    val timestampText =
        remember(widthSizeClass) {
            if (widthSizeClass >= WindowWidthSizeClass.Medium) {
                log.timestamp.formatAsLocalizedDateTime()
            } else {
                log.timestamp.formatAsLocalizedDate() + "\n" + log.timestamp.formatAsLocalizedTime()
            }
        }

    val message =
        when (log.event) {
            is ImportLogEvent.Plural -> resources.getQuantityString(log.event.resId, log.event.quantity, log.event.quantity)
            is ImportLogEvent.SimpleString -> resources.getString(log.event.resId)
            is ImportLogEvent.Raw -> log.event.message
        }

    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            Modifier.secondaryItemAlpha(),
            horizontalAlignment = Alignment.End,
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides
                    MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Normal,
                    ),
            ) {
                Text(timestampText, textAlign = TextAlign.End)
                log.tag?.let { tag -> Text("[$tag]") }
            }
        }

        Text(message, Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun ImportLogBottomSheet(
    onDismissRequest: () -> Unit,
    logs: List<ImportLogObject>,
) {
    val coroutineScope = rememberCoroutineScope()
    val widthSizeClass =
        LocalContext.current
            .findActivity()
            ?.let { calculateWindowSizeClass(it) }
            ?.widthSizeClass
            ?: WindowWidthSizeClass.Compact

    val lazyColumnState = rememberLazyListState()
    val showScrollToTopButton by remember {
        derivedStateOf { lazyColumnState.firstVisibleItemIndex > 0 }
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Scaffold(
            floatingActionButton = {
                AnimatedVisibility(
                    visible = showScrollToTopButton,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch { lazyColumnState.animateScrollToItem(0) }
                        },
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                    }
                }
            },
        ) { innerPadding ->
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .padding(horizontal = MaterialTheme.spacing.pagePadding),
                contentPadding = innerPadding,
                state = lazyColumnState,
            ) {
                if (logs.isEmpty()) {
                    item {
                        EmptyScreen(Modifier.fillMaxSize())
                    }
                }

                items(logs, key = { it.uuid }) {
                    ImportLogObjectUi(
                        it,
                        widthSizeClass,
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = MaterialTheme.spacing.sm)
                            .animateItem(),
                    )
                }
            }
        }
    }
}

private fun previewImportLogObject() =
    ImportLogObject(
        uuid = Uuid.generateV4(),
        timestamp = Instant.fromEpochMilliseconds(0),
        tag = "P-VIEW",
        event = ImportLogEvent.Raw("Wow this is a log wow"),
    )

@Preview
@Composable
private fun ImportLogObjectUiCompactPreview() {
    val log = remember { previewImportLogObject() }

    ArcaeaOfflineTheme {
        Card {
            ImportLogObjectUi(log, WindowWidthSizeClass.Compact)
        }
    }
}

@Preview
@Composable
private fun ImportLogObjectUiMediumPreview() {
    val log = remember { previewImportLogObject() }

    ArcaeaOfflineTheme {
        Card {
            ImportLogObjectUi(log, WindowWidthSizeClass.Medium)
        }
    }
}
