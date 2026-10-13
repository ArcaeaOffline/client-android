package xyz.sevive.arcaeaoffline.ui.screens.settings.about

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.diagnostics.DiagnosticsCollector
import xyz.sevive.arcaeaoffline.ui.components.OperationState

class SettingsAboutViewModel(
    context: Context,
    private val diagnosticsCollector: DiagnosticsCollector,
) : ViewModel() {
    private val appContext = context.applicationContext

    private val _diagnosticsExportState = MutableStateFlow<OperationState?>(null)
    val diagnosticsExportState: StateFlow<OperationState?> = _diagnosticsExportState.asStateFlow()

    fun exportDiagnostics(outputDir: PlatformFile) {
        // atomic check & set progress state
        val previous = _diagnosticsExportState.getAndUpdate { OperationState.InProgress }
        if (previous is OperationState.InProgress) return

        viewModelScope.launch {
            try {
                _diagnosticsExportState.value =
                    try {
                        val target = diagnosticsCollector.exportTo(outputDir)
                        OperationState.Success(
                            appContext.getString(R.string.diagnostics_generated, target.name),
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        OperationState.Failed(e.message)
                    }
            } finally {
                // reset progress if canceled mid-operation
                _diagnosticsExportState.compareAndSet(OperationState.InProgress, null)
            }
        }
    }
}
