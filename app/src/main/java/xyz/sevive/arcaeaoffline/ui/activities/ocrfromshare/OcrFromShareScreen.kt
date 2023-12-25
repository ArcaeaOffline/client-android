package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.models.OcrDependencyViewModel


@Composable
internal fun OcrFromShareReturnToShareAppButton(
    onReturnToShareApp: () -> Unit,
    ocrFromShareViewModel: OcrFromShareViewModel,
    modifier: Modifier = Modifier,
) {
    val appName by ocrFromShareViewModel.shareSourceAppName.collectAsState()
    val appIcon by ocrFromShareViewModel.shareSourceAppIcon.collectAsState()

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
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
internal fun OcrFromShareStayInAppButton(
    onStayInApp: () -> Unit, modifier: Modifier = Modifier
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
    onReturnToShareApp: () -> Unit,
    onStayInApp: () -> Unit,
    ocrDependencyViewModel: OcrDependencyViewModel,
    ocrFromShareViewModel: OcrFromShareViewModel,
) {
    val imageBitmap by ocrFromShareViewModel.imageBitmap.collectAsState()

    Scaffold(topBar = { OcrFromShareTopBar() }) {
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
                OcrFromShareOcrDependencyStatusCard(ocrDependencyViewModel)

                if (imageBitmap != null) {
                    Image(imageBitmap!!, null)
                }
            }

            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
            ) {
                item {
                    OcrFromShareOcrResult(ocrFromShareViewModel)
                }

                item {
                    OcrFromShareActions(ocrFromShareViewModel)
                }

                item {
                    OcrFromShareReturnToShareAppButton(onReturnToShareApp, ocrFromShareViewModel)
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
    ocrDependencyViewModel: OcrDependencyViewModel,
    ocrFromShareViewModel: OcrFromShareViewModel,
) {
    val imageBitmap by ocrFromShareViewModel.imageBitmap.collectAsState()

    Scaffold(topBar = { OcrFromShareTopBar() }) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(dimensionResource(R.dimen.general_page_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
        ) {
            item {
                OcrFromShareOcrDependencyStatusCard(ocrDependencyViewModel)
            }

            if (imageBitmap != null) {
                item {
                    Image(imageBitmap!!, null)
                }
            }

            item {
                OcrFromShareOcrResult(ocrFromShareViewModel)
            }

            item {
                OcrFromShareActions(ocrFromShareViewModel)
            }

            item {
                OcrFromShareReturnToShareAppButton(onReturnToShareApp, ocrFromShareViewModel)
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
    ocrDependencyViewModel: OcrDependencyViewModel,
    ocrFromShareViewModel: OcrFromShareViewModel,
) {
    if (windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium) {
        OcrFromShareScreenContentMedium(
            onReturnToShareApp = onReturnToShareApp,
            onStayInApp = onStayInApp,
            ocrDependencyViewModel = ocrDependencyViewModel,
            ocrFromShareViewModel = ocrFromShareViewModel,
        )
    } else {
        OcrFromShareScreenCompact(
            onReturnToShareApp = onReturnToShareApp,
            onStayInApp = onStayInApp,
            ocrDependencyViewModel = ocrDependencyViewModel,
            ocrFromShareViewModel = ocrFromShareViewModel,
        )
    }
}
