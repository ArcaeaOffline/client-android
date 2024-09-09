package xyz.sevive.arcaeaoffline.ui.activities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.IconRow


@Composable
private fun SectionTitle(text: String) {
    Row(
        Modifier.padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(Modifier.width(4.dp))

        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        HorizontalDivider()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UiTopAppBar(modifier: Modifier = Modifier) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(
                    stringResource(R.string.app_name), style = MaterialTheme.typography.labelLarge
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.error,
            titleContentColor = MaterialTheme.colorScheme.onError,
            navigationIconContentColor = MaterialTheme.colorScheme.onError,
        ),
    )
}

@Composable
fun EmergencyModeActivityUi(
    onSelectOutputDirectory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmergencyModeActivityViewModel,
) {
    val context = LocalContext.current

    val outputDirectory by viewModel.outputDirectory.collectAsStateWithLifecycle()
    val outputDirectoryValid by viewModel.outputDirectoryValid.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        viewModel.reloadPreferencesOnStartUp(context)
    }

    Scaffold(modifier, topBar = { UiTopAppBar() }) { padding ->
        LazyColumn(
            Modifier.padding(padding),
            contentPadding = PaddingValues(dimensionResource(R.dimen.list_padding)),
        ) {
            item {
                SectionTitle(stringResource(R.string.emergency_mode_output_directory_title))
            }
            item {
                Column {
                    IconRow(icon = { Icon(Icons.Default.Code, contentDescription = null) }) {
                        Text(
                            outputDirectory?.uri?.path.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconRow(icon = { Icon(Icons.Outlined.Folder, contentDescription = null) }) {
                        Text(outputDirectory?.name.toString())
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = onSelectOutputDirectory) {
                            Text(stringResource(R.string.emergency_mode_output_directory_select_button))
                        }

                        Spacer(Modifier.width(dimensionResource(R.dimen.list_padding)))

                        CompositionLocalProvider(
                            LocalContentColor provides if (outputDirectoryValid) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        ) {
                            Text(stringResource(R.string.emergency_mode_output_directory_valid_label))
                            Icon(
                                if (outputDirectoryValid) Icons.Default.Check
                                else Icons.Default.Close, contentDescription = null
                            )
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.emergency_mode_ocr_title))
            }
            item {
                Column {
                    Button(onClick = { viewModel.deleteAllOcrDependencies(context) }) {
                        IconRow(icon = {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                        }) {
                            Text(stringResource(R.string.emergency_mode_ocr_delete_all_dependencies_button))
                        }
                    }

                    Button(onClick = { viewModel.deleteOcrQueueDatabase(context) }) {
                        IconRow(icon = {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                        }) {
                            Text(stringResource(R.string.emergency_mode_delete_ocr_queue_db_button))
                        }
                    }
                }
            }

            item {
                SectionTitle(stringResource(R.string.emergency_mode_database_title))
            }
            item {
                Column {
                    Button(
                        onClick = { viewModel.copyDatabase(context) },
                        enabled = outputDirectoryValid,
                    ) {
                        IconRow(icon = {
                            Icon(Icons.Default.FileCopy, contentDescription = null)
                        }) {
                            Text(stringResource(R.string.emergency_mode_database_copy_button))
                        }
                    }
                }
            }
        }
    }
}
