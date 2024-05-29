package xyz.sevive.arcaeaoffline

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.acra.dialog.CrashReportDialogHelper
import xyz.sevive.arcaeaoffline.ui.activities.CrashReportActivityUi
import xyz.sevive.arcaeaoffline.ui.activities.CrashReportViewModel


class CrashReportActivity : ComponentActivity() {
    private lateinit var helper: CrashReportDialogHelper
    private val viewModel get() = viewModels<CrashReportViewModel>().value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        helper = CrashReportDialogHelper(this, intent)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reportProcessed.collect {
                    if (it) finish()
                }
            }
        }

        setContent {
            CrashReportActivityUi(
                helper.reportData,
                onSendReport = { sendReport() },
                onSaveReport = { saveReport() },
                onIgnore = { ignoreReport() },
                viewModel = viewModel
            )
        }
    }

    private fun sendReport() {
        val comment = viewModel.comment.value
        val contact = viewModel.contact.value

        helper.sendCrash(comment = comment, userEmail = contact)
        Toast.makeText(
            this, R.string.crash_report_toast_report_send_attempted, Toast.LENGTH_LONG
        ).show()
        viewModel.reportProcessed()
    }

    private fun saveReport() {
        /* TODO: save report */
        Toast.makeText(this, "Not Implemented", Toast.LENGTH_LONG).show()
//        viewModel.reportProcessed()
    }

    private fun ignoreReport() {
        helper.cancelReports()
        Toast.makeText(this, R.string.crash_report_toast_report_ignored, Toast.LENGTH_LONG).show()
        viewModel.reportProcessed()
    }

    override fun finish() {
        if (!viewModel.reportProcessed.value) ignoreReport()
        super.finish()
    }
}
