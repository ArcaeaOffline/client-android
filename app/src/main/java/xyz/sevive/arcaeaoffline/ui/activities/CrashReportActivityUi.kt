package xyz.sevive.arcaeaoffline.ui.activities

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.acra.ReportField
import org.acra.data.CrashReportData
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.TextWithLabel
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
private fun CrashReportInfoAndActions(
    crashReportData: CrashReportData,
    comment: String?,
    onCommentUpdate: (String) -> Unit,
    contact: String?,
    onContactUpdate: (String) -> Unit,
    onSendReport: () -> Unit,
    onSaveReport: () -> Unit,
    onIgnore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))
    ) {
        TextWithLabel(
            text = crashReportData.getString(ReportField.INSTALLATION_ID) ?: "-",
            label = stringResource(R.string.crash_report_installation_id)
        )
        TextWithLabel(
            text = crashReportData.getString(ReportField.REPORT_ID) ?: "-",
            label = stringResource(R.string.crash_report_report_id)
        )
        TextWithLabel(
            text = crashReportData.getString(ReportField.USER_CRASH_DATE) ?: "UNKNOWN",
            label = stringResource(R.string.crash_report_crash_date)
        )

        OutlinedTextField(
            value = comment ?: "",
            onValueChange = { onCommentUpdate(it) },
            label = { Text(stringResource(R.string.crash_report_textfield_label_comment)) },
            placeholder = { Text(stringResource(R.string.crash_report_textfield_placeholder_comment)) },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contact ?: "",
            onValueChange = { onContactUpdate(it) },
            label = { Text(stringResource(R.string.crash_report_textfield_label_contact)) },
            placeholder = { Text(stringResource(R.string.crash_report_textfield_placeholder_contact)) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))) {
            Button({ onSendReport() }) {
                IconRow(icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = null
                    )
                }) {
                    Text(stringResource(R.string.crash_report_send_button))
                }
            }
            OutlinedButton({ onSaveReport() }) {
                IconRow(icon = { Icon(Icons.Default.Archive, contentDescription = null) }) {
                    Text(stringResource(R.string.crash_report_save_button))
                }
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                { onIgnore() },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                IconRow(icon = { Icon(Icons.Default.Block, contentDescription = null) }) {
                    Text(stringResource(R.string.crash_report_ignore_button))
                }
            }
        }
    }
}

@Composable
private fun CrashReportStacktrace(
    crashReportData: CrashReportData,
    modifier: Modifier = Modifier,
) {
    val stacktrace = crashReportData.getString(ReportField.STACK_TRACE)
        ?: "Unable to get stack trace."

    Text(
        text = stacktrace,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun SadFaceHeader(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            stringResource(R.string.crash_report_title_app_crashed),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(
            Modifier
                .size(24.dp)
                .weight(1f)
        )

        Text(
            ":(",
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashReportActivityUi(
    crashReportData: CrashReportData,
    onSendReport: () -> Unit,
    onSaveReport: () -> Unit,
    onIgnore: () -> Unit,
    viewModel: CrashReportActivityViewModel,
    windowWidthSizeClass: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val comment = uiState.comment
    val contact = uiState.contact

    val expandLayout = windowWidthSizeClass == WindowWidthSizeClass.Expanded

    ArcaeaOfflineTheme {
        Scaffold(
            modifier,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        titleContentColor = MaterialTheme.colorScheme.onError,
                    )
                )
            },
        ) { padding ->
            Surface(
                Modifier
                    .padding(padding)
                    .padding(dimensionResource(R.dimen.page_padding))
                    .fillMaxSize()
            ) {
                if (expandLayout) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.page_padding))
                    ) {
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
                        ) {
                            SadFaceHeader()
                            CrashReportStacktrace(
                                crashReportData,
                                Modifier
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState())
                            )
                        }

                        LazyColumn(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
                        ) {
                            item {
                                Text(
                                    stringResource(R.string.crash_report_send_report_prompt),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            item {
                                CrashReportInfoAndActions(
                                    crashReportData = crashReportData,
                                    comment = comment,
                                    onCommentUpdate = { viewModel.setComment(it) },
                                    contact = contact,
                                    onContactUpdate = { viewModel.setContact(it) },
                                    onSendReport = onSendReport,
                                    onSaveReport = onSaveReport,
                                    onIgnore = onIgnore,
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))) {
                        item {
                            SadFaceHeader()
                        }

                        item {
                            CrashReportStacktrace(
                                crashReportData,
                                Modifier
                                    .height(150.dp)
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState())
                            )
                        }

                        item {
                            HorizontalDivider(Modifier.fillMaxWidth())
                        }

                        item {
                            Text(
                                stringResource(R.string.crash_report_send_report_prompt),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        item {
                            CrashReportInfoAndActions(
                                crashReportData = crashReportData,
                                comment = comment,
                                onCommentUpdate = { viewModel.setComment(it) },
                                contact = contact,
                                onContactUpdate = { viewModel.setContact(it) },
                                onSendReport = onSendReport,
                                onSaveReport = onSaveReport,
                                onIgnore = onIgnore,
                            )
                        }
                    }
                }
            }
        }
    }
}
