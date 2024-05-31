package xyz.sevive.arcaeaoffline

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.acra.dialog.CrashReportDialogHelper
import xyz.sevive.arcaeaoffline.ui.activities.CrashReportActivityUi
import xyz.sevive.arcaeaoffline.ui.activities.CrashReportActivityViewModel


class CrashReportActivity : ComponentActivity() {
    private lateinit var helper: CrashReportDialogHelper
    private val viewModel get() = viewModels<CrashReportActivityViewModel>().value

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        helper = CrashReportDialogHelper(this, intent)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    if (it.reportProcessed) finish()
                }
            }
        }

        setContent {
            CrashReportActivityUi(
                helper.reportData,
                onSendReport = { sendReport() },
                onSaveReport = { saveReport() },
                onIgnore = { ignoreReport() },
                viewModel = viewModel,
                windowWidthSizeClass = calculateWindowSizeClass(activity = this).widthSizeClass,
            )
        }
    }

    private fun sendReport() {
        val comment = viewModel.uiState.value.comment
        val contact = viewModel.uiState.value.contact

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
        if (!viewModel.uiState.value.reportProcessed) ignoreReport()
        super.finish()
    }
}
