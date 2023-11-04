package xyz.sevive.arcaeaoffline

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.acra.ReportField
import org.acra.data.CrashReportData
import org.acra.dialog.CrashReportDialogHelper
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


class CrashReportViewModel : ViewModel() {
    private val _comment = MutableStateFlow<String?>(null)
    val comment: StateFlow<String?> = _comment.asStateFlow()
    private val _contact = MutableStateFlow<String?>(null)
    val contact: StateFlow<String?> = _contact.asStateFlow()

    fun setComment(comment: String?) {
        _comment.value = comment
    }

    fun setContact(contact: String?) {
        _contact.value = contact
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashReportContent(
    crashReportData: CrashReportData,
    onSendReport: (String?, String?) -> Unit,
    onSaveReport: () -> Unit,
    onIgnore: () -> Unit,
    modifier: Modifier = Modifier,
    crashReportViewModel: CrashReportViewModel = viewModel()
) {
    val comment by crashReportViewModel.comment.collectAsState()
    val contact by crashReportViewModel.contact.collectAsState()

    ArcaeaOfflineTheme {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    titleContentColor = MaterialTheme.colorScheme.onError,
                )
            )
        }) { padding ->
            Surface(
                modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                stringResource(R.string.crash_report_title_app_crashed),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(
                                modifier
                                    .defaultMinSize(24.dp, Dp.Unspecified)
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

                    item {
                        Box(modifier.horizontalScroll(rememberScrollState())) {
                            val trace = crashReportData.getString(ReportField.STACK_TRACE)
                            val textStyle = MaterialTheme.typography.labelMedium
                            val textColor = MaterialTheme.colorScheme.error

                            if (trace != null) {
                                val traceLines = trace.lines()
                                val displayTraceLines: List<String> = if (traceLines.size > 5) {
                                    traceLines.subList(0, 5) + listOf("...")
                                } else {
                                    traceLines
                                }

                                Text(
                                    displayTraceLines.joinToString("\n"),
                                    style = textStyle,
                                    color = textColor
                                )
                            } else {
                                Text(
                                    "Unable to get stack trace.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColor
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            stringResource(R.string.crash_report_send_report_prompt),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }


                    item {
                        Column {
                            Text(
                                stringResource(R.string.crash_report_report_id),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Text(crashReportData.getString(ReportField.REPORT_ID) ?: "UNKNOWN")
                        }
                    }

                    item {
                        Column {
                            Text(
                                stringResource(R.string.crash_report_crash_date),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                            Text(
                                crashReportData.getString(ReportField.USER_CRASH_DATE) ?: "UNKNOWN"
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = comment ?: "",
                            onValueChange = { crashReportViewModel.setComment(it) },
                            label = { Text(stringResource(R.string.crash_report_textfield_label_comment)) },
                            placeholder = { Text(stringResource(R.string.crash_report_textfield_placeholder_comment)) },
                            minLines = 2,
                            maxLines = 4,
                            modifier = modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = contact ?: "",
                            onValueChange = { crashReportViewModel.setContact(it) },
                            label = { Text(stringResource(R.string.crash_report_textfield_label_contact)) },
                            placeholder = { Text(stringResource(R.string.crash_report_textfield_placeholder_contact)) },
                            minLines = 2,
                            modifier = modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button({
                                onSendReport(
                                    comment, contact
                                )
                            }) {
                                Text(stringResource(R.string.crash_report_send_button))
                            }
                            OutlinedButton({ onSaveReport() }) {
                                Text(stringResource(R.string.crash_report_save_button))
                            }

                            Spacer(modifier.weight(1f))

                            OutlinedButton(
                                { onIgnore() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.tertiary,
                                )
                            ) {
                                Text(stringResource(R.string.crash_report_ignore_button))
                            }
                        }
                    }
                }
            }
        }
    }
}

class CustomAcraCrashReportActivity : ComponentActivity() {
    private lateinit var helper: CrashReportDialogHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        helper = CrashReportDialogHelper(this, intent)

        onBackPressedDispatcher.addCallback {
            ignoreReport()
        }

        Log.i("CDate", helper.reportData.getString(ReportField.USER_CRASH_DATE) ?: "null")

        setContent {
            CrashReportContent(
                helper.reportData,
                onSendReport = { comment, contact -> sendReport(comment, contact) },
                onSaveReport = { saveReport() },
                onIgnore = { ignoreReport() },
            )
        }
    }

    private fun sendReport(comment: String?, contact: String?) {
        helper.sendCrash(comment, contact)
        Toast.makeText(
            this, R.string.crash_report_toast_report_send_attempted, Toast.LENGTH_LONG
        ).show()
        finish()
    }

    private fun saveReport() {
        Toast.makeText(this, "Not Implemented", Toast.LENGTH_LONG).show()/* TODO: save report */
//        finish()
    }

    private fun ignoreReport() {
        helper.cancelReports()
        Toast.makeText(this, R.string.crash_report_toast_report_ignored, Toast.LENGTH_LONG).show()
        finish()
    }
}
