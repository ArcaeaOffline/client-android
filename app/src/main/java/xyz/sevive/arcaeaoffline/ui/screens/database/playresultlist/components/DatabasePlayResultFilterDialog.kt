package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalog
import xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.PlayResultFilter

private val FilterDialogCompactMaxWidth = 480.dp

private const val FILTER_DIALOG_COMPACT_WIDTH_FRACTION = 0.92f

private val FilterDialogCompactCorner = 28.dp

private const val PAGE_TRANSITION_DURATION_MILLIS = 220

private val FilterCompactScreens: List<FilterPicker?> =
    listOf<FilterPicker?>(null) + FilterPicker.entries.filter { it.sizeClass == FilterSizeClass.COMPACT }

/**
 * Filter dialog: a compact facet card. A picker with many options (packs)
 * expands to full screen; the difficulty picker only grows the card in height,
 * keeping the compact width, corners and elevation.
 *
 * Filters apply live. [onDismissRequest] is also used for back: with a picker
 * open it closes the picker instead of the dialog.
 */
@Composable
fun DatabasePlayResultFilterDialog(
    filter: PlayResultFilter,
    catalog: ArcaeaCatalog,
    songPlayResultCounts: Map<String, Int>,
    ratingClassCounts: Map<ArcaeaRatingClass, Int>,
    resultCount: Int,
    onFilterChange: (PlayResultFilter) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var picker by rememberSaveable { mutableStateOf<FilterPicker?>(null) }

    fun handleBack() {
        if (picker == null) onDismissRequest() else picker = null
    }

    val state =
        FilterDialogState(
            filter = filter,
            catalog = catalog,
            songPlayResultCounts = songPlayResultCounts,
            ratingClassCounts = ratingClassCounts,
            resultCount = resultCount,
            onFilterChange = onFilterChange,
        )

    Dialog(
        onDismissRequest = { handleBack() },
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        BoxWithConstraints(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val availableWidth = maxWidth
            val availableHeight = maxHeight
            val compactWidth =
                minOf(availableWidth * FILTER_DIALOG_COMPACT_WIDTH_FRACTION, FilterDialogCompactMaxWidth)
            val sizeClass = picker?.sizeClass ?: FilterSizeClass.COMPACT
            val fullScreen = sizeClass == FilterSizeClass.FULL
            val density = LocalDensity.current
            val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
            val layoutDirection = LocalLayoutDirection.current

            val startInset by animateDpAsState(
                targetValue = if (fullScreen) safeInsets.calculateStartPadding(layoutDirection) else 0.dp,
                animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                label = "filterStartInset",
            )
            val topInset by animateDpAsState(
                targetValue = if (fullScreen) safeInsets.calculateTopPadding() else 0.dp,
                animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                label = "filterTopInset",
            )
            val endInset by animateDpAsState(
                targetValue = if (fullScreen) safeInsets.calculateEndPadding(layoutDirection) else 0.dp,
                animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                label = "filterEndInset",
            )
            val bottomInset by animateDpAsState(
                targetValue = if (fullScreen) safeInsets.calculateBottomPadding() else 0.dp,
                animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                label = "filterBottomInset",
            )

            // Measure every compact screen once so pickers can animate between
            // their concrete sizes and the full-screen picker size.
            var compactSizes by remember { mutableStateOf<Map<FilterPicker?, IntSize>>(emptyMap()) }

            val initialCompactSize = compactSizes[picker]

            // Seed the animations with the measured size so the first frame after
            // measuring does not animate in from zero.
            val animatedWidth =
                remember(compactSizes) {
                    Animatable(initialCompactSize?.let { with(density) { it.width.toDp().value } } ?: 0f)
                }
            val animatedHeight =
                remember(compactSizes) {
                    Animatable(initialCompactSize?.let { with(density) { it.height.toDp().value } } ?: 0f)
                }

            LaunchedEffect(picker, compactSizes, availableWidth, availableHeight) {
                if (compactSizes.isEmpty()) return@LaunchedEffect

                val targetWidth = if (fullScreen) availableWidth.value else compactWidth.value
                val targetHeight =
                    when (sizeClass) {
                        FilterSizeClass.FULL -> {
                            availableHeight.value
                        }

                        FilterSizeClass.COMPACT -> {
                            val screenSize = compactSizes[picker] ?: return@LaunchedEffect
                            with(density) { screenSize.height.toDp().value }
                        }
                    }

                launch { animatedWidth.animateTo(targetWidth, tween(PAGE_TRANSITION_DURATION_MILLIS)) }
                launch { animatedHeight.animateTo(targetHeight, tween(PAGE_TRANSITION_DURATION_MILLIS)) }
            }

            val corner by animateDpAsState(
                targetValue = if (fullScreen) 0.dp else FilterDialogCompactCorner,
                animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                label = "filterCorner",
            )
            val elevation by animateDpAsState(
                targetValue = if (fullScreen) 0.dp else 6.dp,
                animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                label = "filterElevation",
            )

            val measuring = sizeClass == FilterSizeClass.COMPACT && FilterCompactScreens.any { it !in compactSizes }
            val sizeModifier =
                when {
                    measuring -> Modifier.width(compactWidth).wrapContentHeight()
                    compactSizes.isEmpty() -> Modifier.fillMaxSize()
                    else -> Modifier.width(animatedWidth.value.dp).height(animatedHeight.value.dp)
                }

            val screenContent: @Composable (FilterPicker?, Boolean) -> Unit = { shownPicker, fill ->
                FilterScreen(
                    state = state,
                    picker = shownPicker,
                    fill = fill,
                    onOpenPicker = { picker = it },
                    onBack = { handleBack() },
                    onDismiss = onDismissRequest,
                )
            }

            Surface(
                modifier = sizeModifier,
                shape = RoundedCornerShape(corner),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = elevation,
                shadowElevation = elevation,
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .then(if (measuring) Modifier else Modifier.fillMaxSize())
                        .padding(start = startInset, top = topInset, end = endInset, bottom = bottomInset),
                ) {
                    if (measuring) {
                        MeasureCompactScreens(
                            compactWidth = compactWidth,
                            screens = FilterCompactScreens,
                            visiblePicker = picker,
                            content = { shownPicker -> screenContent(shownPicker, false) },
                            onSize = { shownPicker, size -> compactSizes = compactSizes + (shownPicker to size) },
                        )
                    } else {
                        Crossfade(
                            targetState = picker,
                            animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                            label = "filterScreen",
                        ) { shownPicker ->
                            screenContent(shownPicker, true)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Measures the compact screens at [compactWidth].
 *
 * All screens are laid out; only [visiblePicker] draws, the rest are composed
 * with zero alpha so their heights are known before they are shown. With every
 * height known, picker changes animate without a measure-then-snap frame.
 */
@Composable
private fun MeasureCompactScreens(
    compactWidth: Dp,
    screens: List<FilterPicker?>,
    visiblePicker: FilterPicker?,
    content: @Composable (FilterPicker?) -> Unit,
    onSize: (FilterPicker?, IntSize) -> Unit,
) {
    Layout(
        content = {
            screens.forEach { screen ->
                Box(
                    Modifier
                        .then(if (screen == visiblePicker) Modifier else Modifier.alpha(0f))
                        .onSizeChanged { onSize(screen, it) },
                ) { content(screen) }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) { measurables, constraints ->
        val widthPx = compactWidth.roundToPx()
        val pageConstraints =
            Constraints(
                minWidth = widthPx,
                maxWidth = widthPx,
                minHeight = 0,
                maxHeight = constraints.maxHeight,
            )

        val placeables = screens.indices.map { measurables[it].measure(pageConstraints) }
        val visiblePlaceable = placeables[screens.indexOf(visiblePicker)]

        layout(visiblePlaceable.width, visiblePlaceable.height) {
            placeables.forEach { it.placeRelative(0, 0) }
        }
    }
}
