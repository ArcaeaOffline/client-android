package xyz.sevive.arcaeaoffline.ui.activities

import android.content.Context
import android.net.Uri
import android.text.format.Formatter
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.database.OcrQueueDatabase
import xyz.sevive.arcaeaoffline.datastore.EmergencyModePreferencesRepository


class EmergencyModeActivityViewModel(
    private val preferencesRepository: EmergencyModePreferencesRepository,
) : ViewModel() {
    companion object {
        private const val TEST_FILENAME = "test_write"
    }

    fun reloadPreferencesOnStartUp(context: Context) {
        viewModelScope.launch {
            val preferences = preferencesRepository.preferencesFlow.firstOrNull() ?: return@launch

            if (preferences.hasLastOutputDirectory()) {
                val lastOutputDirectory = preferences.lastOutputDirectory
                if (lastOutputDirectory.isNotEmpty()) {
                    DocumentFile.fromTreeUri(context, Uri.parse(lastOutputDirectory))?.let {
                        setOutputDirectory(it)
                    }
                }
            }
        }
    }

    private val _outputDirectory = MutableStateFlow<DocumentFile?>(null)
    val outputDirectory = _outputDirectory.asStateFlow()

    val outputDirectoryValid = outputDirectory.map { outputDirectoryValidResultProducer() }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 1000L),
        initialValue = false
    )

    fun setOutputDirectory(documentFile: DocumentFile) {
        _outputDirectory.value = documentFile

        viewModelScope.launch {
            preferencesRepository.updateLastOutputDirectory(documentFile)
        }
    }

    fun outputDirectoryValidResultProducer(): Boolean {
        outputDirectory.value?.exists() ?: return false

        outputDirectory.value?.createFile("unknown/unknown", TEST_FILENAME)
        val result = outputDirectory.value?.findFile(TEST_FILENAME)?.exists() ?: false
        if (result) {
            outputDirectory.value?.findFile(TEST_FILENAME)?.delete()
        }
        return result
    }

    fun deleteAllOcrDependencies(context: Context) {
        val paths = OcrDependencyPaths(context)
        paths.knnModelFile.delete()
        paths.phashDatabaseFile.delete()
    }

    fun deleteOcrQueueDatabase(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                context.deleteDatabase(OcrQueueDatabase.DATABASE_FILENAME)
            }.invokeOnCompletion {
                launch(Dispatchers.Main) {
                    val message = if (it == null) {
                        context.getString(R.string.general_delete)
                    } else {
                        it::class.simpleName ?: "ERROR"
                    }

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun copyDatabase(context: Context) {
        val originalDatabaseFile = context.getDatabasePath(ArcaeaOfflineDatabase.DATABASE_FILENAME)
        var outputFile: DocumentFile? = null
        viewModelScope.launch(Dispatchers.IO) {
            val backupFileName = "arcaea_offline_${System.currentTimeMillis()}.db"
            val backupFile =
                outputDirectory.value?.createFile("application/octet-stream", backupFileName)
                    ?: return@launch

            val backupFileStream =
                context.contentResolver.openOutputStream(backupFile.uri) ?: return@launch

            backupFileStream.use { IOUtils.copy(originalDatabaseFile.inputStream(), it) }

            outputFile = backupFile
        }.invokeOnCompletion {
            outputFile?.let {
                val fileSizeReadable = Formatter.formatShortFileSize(context, it.length())
                Toast.makeText(
                    context,
                    context.getString(
                        R.string.emergency_mode_database_copied_message, it.name, fileSizeReadable
                    ),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }
}
