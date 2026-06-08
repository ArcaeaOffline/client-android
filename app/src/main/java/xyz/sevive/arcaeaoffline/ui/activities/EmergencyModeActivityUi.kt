package xyz.sevive.arcaeaoffline.ui.activities

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.context.persistUriPermissions
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.ListGroupHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UiTopAppBar(modifier: Modifier = Modifier) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(stringResource(R.string.emergency_mode_title))
            }
        },
        navigationIcon = {
            Icon(
                painterResource(R.drawable.ic_activity_emergency_mode),
                contentDescription = null,
            )
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.error,
                titleContentColor = MaterialTheme.colorScheme.onError,
                navigationIconContentColor = MaterialTheme.colorScheme.onError,
            ),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmergencyModeActivityUi(
    modifier: Modifier = Modifier,
    viewModel: EmergencyModeActivityViewModel,
) {
    val context = LocalContext.current

    val outputDirectory by viewModel.outputDirectory.collectAsStateWithLifecycle()
    val outputDirectoryValid by viewModel.outputDirectoryValid.collectAsStateWithLifecycle()

    val dirPicker =
        rememberDirectoryPickerLauncher { dir ->
            dir?.let {
                context.persistUriPermissions(
                    it.path.toUri(),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
                viewModel.setOutputDirectory(it)
            }
        }

    LaunchedEffect(key1 = Unit) {
        viewModel.reloadPreferencesOnStartUp()
    }

    Scaffold(modifier, topBar = { UiTopAppBar() }) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .consumeWindowInsets(padding),
            contentPadding =
                PaddingValues(
                    top = padding.calculateTopPadding() + dimensionResource(R.dimen.list_padding),
                    bottom = padding.calculateBottomPadding() + dimensionResource(R.dimen.list_padding),
                    start = dimensionResource(R.dimen.list_padding),
                    end = dimensionResource(R.dimen.list_padding),
                ),
        ) {
            item {
                ListGroupHeader(stringResource(R.string.emergency_mode_output_directory_title))
            }
            item {
                Column {
                    IconRow {
                        Icon(Icons.Default.Code, contentDescription = null)
                        Text(
                            outputDirectory?.path.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconRow {
                        Icon(Icons.Outlined.Folder, contentDescription = null)
                        Text(outputDirectory?.name.toString())
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { dirPicker.launch() }) {
                            Text(stringResource(R.string.emergency_mode_output_directory_select_button))
                        }

                        Spacer(Modifier.width(dimensionResource(R.dimen.list_padding)))

                        CompositionLocalProvider(
                            LocalContentColor provides
                                if (outputDirectoryValid) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                        ) {
                            Text(stringResource(R.string.emergency_mode_output_directory_valid_label))
                            Icon(
                                if (outputDirectoryValid) {
                                    Icons.Default.Check
                                } else {
                                    Icons.Default.Close
                                },
                                contentDescription = null,
                            )
                        }
                    }
                }
            }

            item {
                ListGroupHeader(stringResource(R.string.emergency_mode_ocr_title))
            }
            item {
                FlowRow {
                    Button(onClick = { viewModel.deleteAllOcrDependencies() }) {
                        IconRow {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Text(stringResource(R.string.emergency_mode_ocr_delete_all_dependencies_button))
                        }
                    }

                    Button(onClick = { viewModel.deleteOcrQueueDatabase(context) }) {
                        IconRow {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Text(stringResource(R.string.emergency_mode_delete_ocr_queue_db_button))
                        }
                    }
                }
            }

            item {
                ListGroupHeader(stringResource(R.string.emergency_mode_database_title))
            }
            item {
                FlowRow {
                    Button(
                        onClick = { viewModel.copyDatabase(context) },
                        enabled = outputDirectoryValid,
                    ) {
                        IconRow {
                            Icon(Icons.Default.FileCopy, contentDescription = null)
                            Text(stringResource(R.string.emergency_mode_database_copy_button))
                        }
                    }
                }
            }
        }
    }
}
