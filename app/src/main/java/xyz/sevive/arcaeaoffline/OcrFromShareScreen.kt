package xyz.sevive.arcaeaoffline

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKnnModelStatus
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyPhashDatabaseStatus
import xyz.sevive.arcaeaoffline.ui.components.scoreeditor.ScoreEditor
import xyz.sevive.arcaeaoffline.ui.components.scoreeditor.ScoreEditorViewModel
import xyz.sevive.arcaeaoffline.ui.models.OcrDependencyViewModel
import xyz.sevive.arcaeaoffline.ui.utils.findActivity


@Composable
internal fun AppIconAndLabelButton(
    onClick: () -> Unit,
    appIcon: @Composable () -> Unit,
    appLabel: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actionText: (@Composable () -> Unit)? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    trailingIcon: @Composable () -> Unit = { Icon(Icons.AutoMirrored.Default.ArrowForward, null) },
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.action_button_icon_text_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            appIcon()

            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.general_icon_text_padding)),
            ) {
                appLabel()

                if (actionText != null) {
                    actionText()
                }
            }

            trailingIcon()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OcrFromShareScreenTopBar() {
    TopAppBar(
        title = { Text(text = stringResource(R.string.title_activity_ocr_from_share)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
internal fun OcrFromShareScreenOcrDependencyStatusCard(
    ocrDependencyViewModel: OcrDependencyViewModel,
    modifier: Modifier = Modifier,
) {
    val knnModelState by ocrDependencyViewModel.knnModelState.collectAsState()
    val phashDatabaseState by ocrDependencyViewModel.phashDatabaseState.collectAsState()

    Card(modifier) {
        OcrDependencyKnnModelStatus(state = knnModelState)
        OcrDependencyPhashDatabaseStatus(state = phashDatabaseState)
    }
}

@Composable
internal fun OcrFromShareScreenResult(
    ocrFromShareViewModel: OcrFromShareViewModel
) {
    val score by ocrFromShareViewModel.score.collectAsState()
    val exception by ocrFromShareViewModel.exception.collectAsState()
    val chart by ocrFromShareViewModel.chart.collectAsState()

    Box(Modifier.padding(dimensionResource(R.dimen.general_page_padding), 0.dp)) {
        if (score != null) {
            ArcaeaScoreCard(score = score!!, chart = chart)
        } else if (exception != null) {
            Text(
                exception!!.message ?: exception.toString(),
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            Text(stringResource(R.string.ocr_from_share_waiting_result))
        }
    }
}

@Composable
internal fun OcrFromShareScreenActions(
    ocrFromShareViewModel: OcrFromShareViewModel,
    scoreEditorExpanded: Boolean,
    scoreEditorViewModel: ScoreEditorViewModel = viewModel(),
) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val activity = context.findActivity()
    val sourcePackageName = if (activity == null) {
        null
    } else if (activity.intent.`package` != null) {
        activity.intent.`package`
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        activity.referrer?.toString()?.replace("android-app://", "")
    } else {
        null
    }

    var showScoreEditorDialog by remember { mutableStateOf(false) }

    val score by ocrFromShareViewModel.score.collectAsState()
    val scoreSaved by ocrFromShareViewModel.scoreSaved.collectAsState()
    val scoreCached by ocrFromShareViewModel.scoreCached.collectAsState()

    val showScoreSavedBanner = score != null && !scoreSaved

    AnimatedContent(
        targetState = showScoreSavedBanner,
        transitionSpec = {
            slideInVertically { -it }.togetherWith(slideOutVertically { -it } + fadeOut())
        },
        label = "",
    ) {
        if (it) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))) {
                Button(
                    onClick = { coroutineScope.launch { ocrFromShareViewModel.saveScore() } },
                    enabled = score != null && !scoreSaved,
                ) {
                    IconRow(icon = { Icon(Icons.Default.Save, null) }) {
                        Text(stringResource(R.string.general_save))
                    }
                }

                Button(
                    onClick = {
                        if (score != null) {
                            scoreEditorViewModel.setArcaeaScore(score!!)
                            showScoreEditorDialog = true
                        }
                    },
                    enabled = score != null && !scoreSaved,
                ) {
                    IconRow(icon = { Icon(Icons.Default.Edit, null) }) {
                        Text(stringResource(R.string.general_edit))
                    }
                }

                Spacer(Modifier.weight(1f))

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            ocrFromShareViewModel.cacheScore(sourcePackageName)
                        }
                    },
                    enabled = score != null && !scoreSaved && !scoreCached,
                ) {
                    IconRow(icon = { Icon(Icons.Default.Archive, null) }) {
                        Text(stringResource(R.string.ocr_from_share_cache_score_button))
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.ocr_from_share_score_saved),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }

    if (showScoreEditorDialog) {
        Dialog(
            onDismissRequest = { showScoreEditorDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface {
                LazyColumn(Modifier.padding(dimensionResource(R.dimen.general_page_padding))) {
                    item {
                        ScoreEditor(
                            onScoreCommit = {
                                ocrFromShareViewModel.setScore(it)
                                showScoreEditorDialog = false
                            },
                            viewModel = scoreEditorViewModel,
                            expanded = scoreEditorExpanded,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun OcrFromShareScreenReturnToShareAppButton(
    appName: String?,
    appIcon: ImageBitmap?,
    onReturnToShareApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconSize = Icons.Default.Apps.defaultHeight

    AppIconAndLabelButton(
        onClick = { onReturnToShareApp() },
        appIcon = {
            if (appIcon != null) {
                Image(appIcon, null, Modifier.size(iconSize))
            } else {
                Icon(Icons.Default.Apps, null, Modifier.size(iconSize))
            }
        },
        appLabel = {
            if (appName != null) {
                Text(appName)
            }
        },
        actionText = { Text(stringResource(R.string.ocr_from_share_return_to_share_source)) },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
internal fun OcrFromShareScreenStayInAppButton(
    onStayInApp: () -> Unit, modifier: Modifier = Modifier
) {
    AppIconAndLabelButton(
        onClick = { onStayInApp() },
        appIcon = {
            Icon(Icons.Default.Dashboard, null)
        },
        appLabel = {
            Text(
                String.format(
                    stringResource(R.string.ocr_from_share_stay_in_app),
                    stringResource(R.string.app_name),
                )
            )
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        modifier = modifier,
    )
}

@Composable
internal fun OcrFromShareScreenContentMedium(
    appName: String?,
    appIcon: ImageBitmap?,
    onReturnToShareApp: () -> Unit,
    onStayInApp: () -> Unit,
    ocrDependencyViewModel: OcrDependencyViewModel,
    ocrFromShareViewModel: OcrFromShareViewModel,
) {
    val imageBitmap by ocrFromShareViewModel.imageBitmap.collectAsState()

    Scaffold(topBar = { OcrFromShareScreenTopBar() }) {
        Row(
            Modifier
                .padding(it)
                .padding(dimensionResource(R.dimen.general_page_padding)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.general_page_padding))
        ) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
            ) {
                OcrFromShareScreenOcrDependencyStatusCard(ocrDependencyViewModel)

                if (imageBitmap != null) {
                    Image(imageBitmap!!, null)
                }
            }

            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
            ) {
                item {
                    OcrFromShareScreenResult(ocrFromShareViewModel)
                }

                item {
                    OcrFromShareScreenActions(ocrFromShareViewModel, scoreEditorExpanded = true)
                }

                item {
                    OcrFromShareScreenReturnToShareAppButton(
                        appName = appName,
                        appIcon = appIcon,
                        onReturnToShareApp,
                    )
                }

                item {
                    OcrFromShareScreenStayInAppButton(onStayInApp)
                }
            }
        }
    }
}

@Composable
fun OcrFromShareScreenCompact(
    appName: String?,
    appIcon: ImageBitmap?,
    onReturnToShareApp: () -> Unit,
    onStayInApp: () -> Unit,
    ocrDependencyViewModel: OcrDependencyViewModel,
    ocrFromShareViewModel: OcrFromShareViewModel,
) {
    val imageBitmap by ocrFromShareViewModel.imageBitmap.collectAsState()

    Scaffold(topBar = { OcrFromShareScreenTopBar() }) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(dimensionResource(R.dimen.general_page_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
        ) {
            item {
                OcrFromShareScreenOcrDependencyStatusCard(ocrDependencyViewModel)
            }

            if (imageBitmap != null) {
                item {
                    Image(imageBitmap!!, null)
                }
            }

            item {
                OcrFromShareScreenResult(ocrFromShareViewModel)
            }

            item {
                OcrFromShareScreenActions(ocrFromShareViewModel, scoreEditorExpanded = false)
            }

            item {
                OcrFromShareScreenReturnToShareAppButton(
                    appName = appName,
                    appIcon = appIcon,
                    onReturnToShareApp,
                )
            }

            item {
                OcrFromShareScreenStayInAppButton(onStayInApp)
            }
        }
    }
}

@Composable
fun OcrFromShareScreen(
    windowSizeClass: WindowSizeClass,
    appName: String?,
    appIcon: ImageBitmap?,
    onReturnToShareApp: () -> Unit,
    onStayInApp: () -> Unit,
    ocrDependencyViewModel: OcrDependencyViewModel,
    ocrFromShareViewModel: OcrFromShareViewModel,
) {
    if (windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium) {
        OcrFromShareScreenContentMedium(
            appName = appName,
            appIcon = appIcon,
            onReturnToShareApp = onReturnToShareApp,
            onStayInApp = onStayInApp,
            ocrDependencyViewModel = ocrDependencyViewModel,
            ocrFromShareViewModel = ocrFromShareViewModel,
        )
    } else {
        OcrFromShareScreenCompact(
            appName = appName,
            appIcon = appIcon,
            onReturnToShareApp = onReturnToShareApp,
            onStayInApp = onStayInApp,
            ocrDependencyViewModel = ocrDependencyViewModel,
            ocrFromShareViewModel = ocrFromShareViewModel,
        )
    }
}
