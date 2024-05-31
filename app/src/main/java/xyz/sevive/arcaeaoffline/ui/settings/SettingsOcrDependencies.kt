package xyz.sevive.arcaeaoffline.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.GlobalOcrDependencyHelper
import xyz.sevive.arcaeaoffline.ui.components.ActionButton
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaButton
import xyz.sevive.arcaeaoffline.ui.components.TitleOutlinedCard
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKnnModelStatus
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyPhashDatabaseStatus


@Composable
fun SettingsOcrDependencies(viewModel: SettingsViewModel) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    val expandArrowRotateDegree: Float by animateFloatAsState(
        if (expanded) 0f else -90f, label = "expandArrowRotate"
    )

    val context = LocalContext.current

    val knnModelState = GlobalOcrDependencyHelper.knnModelState.collectAsStateWithLifecycle()
    val phashDatabaseState =
        GlobalOcrDependencyHelper.phashDatabaseState.collectAsStateWithLifecycle()

    val importKnnLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.importKnnModel(it, context) } }

    val importPhashDbLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.importPhashDatabase(it, context) } }

    TitleOutlinedCard(title = {
        ActionButton(
            onClick = { expanded = !expanded },
            title = stringResource(R.string.settings_ocr_dependencies_title),
            shape = settingsTitleActionCardShape(),
            headSlot = { Icon(Icons.Default.Api, null) },
            tailSlot = {
                Icon(
                    Icons.Filled.ExpandMore, null, Modifier.rotate(expandArrowRotateDegree)
                )
            },
        )
    }) { padding ->
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(padding)) {
                OcrDependencyKnnModelStatus(state = knnModelState.value)
                Button(onClick = { importKnnLauncher.launch("*/*") }) {
                    Text(stringResource(R.string.general_import))
                }

                OcrDependencyPhashDatabaseStatus(state = phashDatabaseState.value)
                Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))) {
                    Button(onClick = { importPhashDbLauncher.launch("*/*") }) {
                        Text(stringResource(R.string.general_import))
                    }

                    ArcaeaButton(
                        onClick = { viewModel.buildPhashDatabaseFromArcaea(context) },
                        state = viewModel.buildPhashDatabaseFromArcaeaButtonState(context),
                    ) {
                        Text(stringResource(R.string.settings_ocr_phash_database_build_from_arcaea))
                    }
                }
            }
        }
    }

    val phashDatabaseBuildProgress by viewModel.phashDatabaseBuildProgress.collectAsStateWithLifecycle()
    if (phashDatabaseBuildProgress != null) {
        val progress = phashDatabaseBuildProgress!!.progress
        val total = phashDatabaseBuildProgress!!.total

        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            icon = { Icon(painterResource(R.drawable.ic_database), null) },
            text = {
                Column {
                    if (total > -1) {
                        Text("${progress}/$total")
                        LinearProgressIndicator(progress = { progress.toFloat() / total })
                    } else {
                        Text(stringResource(R.string.general_please_wait))
                        LinearProgressIndicator()
                    }
                }
            },
        )
    }
}
