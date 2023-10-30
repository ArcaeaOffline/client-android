package xyz.sevive.arcaeaoffline.ui.components.ocr

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material.icons.twotone.Error
import androidx.compose.material.icons.twotone.QuestionMark
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ocr.ImagePhashDatabase
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import java.io.File

enum class OcrDependencyType {
    KNN_MODEL, PHASH_DB
}

data class OcrDependencyItem(
    val type: OcrDependencyType, val file: File
)

enum class OcrDependencyStatus {
    OK, ERROR, WARN, UNKNOWN
}

@Composable
fun OcrDependencyItemStatus(
    title: @Composable ColumnScope.() -> Unit,
    label: @Composable ColumnScope.() -> Unit,
    status: OcrDependencyStatus,
    details: (@Composable AnimatedVisibilityScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var showDetails by remember { mutableStateOf(false) }
    val expandArrowRotateDegree: Float by animateFloatAsState(
        if (showDetails) 180f else 0f, label = "expandArrowRotate"
    )

    Surface(modifier.padding(8.dp)) {
        Column(modifier) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier.weight(1f)) {
                    title()
                    label()
                }

                var statusIcon = Icons.TwoTone.QuestionMark
                var statusText = "UNKNOWN"
                var statusColor = MaterialTheme.colorScheme.secondary

                when (status) {
                    OcrDependencyStatus.OK -> {
                        statusIcon = Icons.TwoTone.CheckCircle
                        statusText = "OK"
                        statusColor = MaterialTheme.colorScheme.primary
                    }

                    OcrDependencyStatus.WARN -> {
                        statusIcon = Icons.TwoTone.Warning
                        statusText = "WARN"
                        statusColor = MaterialTheme.colorScheme.tertiary
                    }

                    OcrDependencyStatus.ERROR -> {
                        statusIcon = Icons.TwoTone.Error
                        statusText = "ERROR"
                        statusColor = MaterialTheme.colorScheme.error
                    }

                    else -> {}
                }

                Row(modifier, verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        statusIcon,
                        "OCR dependency status icon",
                        modifier.padding(end = 2.dp),
                        statusColor
                    )
                    Text(
                        statusText, color = statusColor, style = MaterialTheme.typography.labelLarge
                    )
                }

                if (details != null) {
                    TextButton(onClick = { showDetails = !showDetails }) {
                        Icon(
                            Icons.Default.ExpandMore, "", modifier.rotate(expandArrowRotateDegree)
                        )
                    }
                }
            }
            if (details != null) {
                AnimatedVisibility(visible = showDetails, content = details)
            }
        }
    }
}

@Composable
fun LabelMediumText(
    string: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified
) {
    Text(string, modifier, color = color, style = MaterialTheme.typography.labelMedium)
}

@Composable
fun OcrDependencyKnnModelStatus(item: OcrDependencyItem, modifier: Modifier = Modifier) {
    var model: KNearest? = null
    var error: Exception? = null
    try {
        model = KNearest.load(item.file.path)
    } catch (e: Exception) {
        error = e
        Log.e("OCR Dependency", "Error loading ${item.file} as a knn model", e)
    }

    OcrDependencyItemStatus(
        title = { Text(stringResource(R.string.ocr_dependency_knnModel)) },
        label = {
            if (error == null && model != null) {
                LabelMediumText(
                    "varCount ${model.varCount}",
                    modifier = modifier,
                    color = MaterialTheme.colorScheme.secondary
                )

            }
        },
        status = if (error != null) {
            OcrDependencyStatus.ERROR
        } else if (model != null && model.varCount == 81) {
            OcrDependencyStatus.OK
        } else if (model != null) {
            OcrDependencyStatus.WARN
        } else {
            OcrDependencyStatus.UNKNOWN
        },
        details = {
            if (error != null) {
                error.localizedMessage?.let { LabelMediumText(it) }
            } else if (model != null) {
                LabelMediumText(model.toString())
            }
        },
        modifier = modifier,
    )
}

@Composable
fun OcrDependencyPhashDatabaseStatus(item: OcrDependencyItem, modifier: Modifier = Modifier) {
    var db: ImagePhashDatabase? = null
    var error: Exception? = null
    try {
        db = ImagePhashDatabase(item.file.path)
    } catch (e: Exception) {
        error = e
        Log.e("OCR Dependency", "Error loading image phash database", e)
    }

    OcrDependencyItemStatus(
        title = { Text(stringResource(R.string.ocr_dependency_phashDatabase)) },
        label = {
            if (error == null && db != null) {
                LabelMediumText("J${db.jacketHashes.size} PI${db.partnerIconHashes.size}")
            }
        },
        status = if (error == null) OcrDependencyStatus.OK else OcrDependencyStatus.ERROR,
        modifier = modifier
    )
}

@Composable
fun OcrDependencyStatus(modifier: Modifier = Modifier, vararg dependencies: OcrDependencyItem) {
    for (dependency in dependencies) {
        when (dependency.type) {
            OcrDependencyType.KNN_MODEL -> OcrDependencyKnnModelStatus(
                dependency, modifier = modifier
            )

            OcrDependencyType.PHASH_DB -> OcrDependencyPhashDatabaseStatus(
                dependency, modifier = modifier
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun OcrDependencyItemStatusPreview(modifier: Modifier = Modifier) {
    ArcaeaOfflineTheme {
        Column {
            OcrDependencyItemStatus(
                title = { Text("Test Dep 1") },
                label = { Text("OK Preview", style = MaterialTheme.typography.labelMedium) },
                status = OcrDependencyStatus.OK,
                details = {
                    Text(
                        "and this is ok i think there isn't much details",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                modifier = modifier
            )
            OcrDependencyItemStatus(
                title = { Text("Test Dep 2") },
                label = { Text("WARN Preview", style = MaterialTheme.typography.labelMedium) },
                status = OcrDependencyStatus.WARN,
                details = {
                    Text(
                        "and this is warn i think there isn't much details",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                modifier = modifier
            )
            OcrDependencyItemStatus(
                title = { Text("Test Dep 3") },
                label = { Text("ERR Preview", style = MaterialTheme.typography.labelMedium) },
                status = OcrDependencyStatus.ERROR,
                details = {
                    Text(
                        "a".repeat(75) + "ERROR NO" + "O".repeat(283),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                modifier = modifier
            )
        }
    }
}
