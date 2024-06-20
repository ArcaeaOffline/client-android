package xyz.sevive.arcaeaoffline.ui.activities

import android.content.Context
import android.text.format.Formatter
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths


class EmergencyModeActivityViewModel : ViewModel() {
    companion object {
        private const val TEST_FILENAME = "test_write"
    }

    private val _outputDirectory = MutableStateFlow<DocumentFile?>(null)
    val outputDirectory = _outputDirectory.asStateFlow()

    val outputDirectoryValid = flow { emit(outputDirectoryValidResultProducer()) }

    fun setOutputDirectory(documentFile: DocumentFile) {
        _outputDirectory.value = documentFile
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

    fun copyDatabase(context: Context) {
        val originalDatabaseFile = context.getDatabasePath(ArcaeaOfflineDatabase.DATABASE_NAME)
        var outputFile: DocumentFile? = null
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val backupFileName = "arcaea_offline_${System.currentTimeMillis()}.db"
                val backupFile =
                    outputDirectory.value?.createFile("application/octet-stream", backupFileName)
                        ?: return@withContext

                val backupFileStream =
                    context.contentResolver.openOutputStream(backupFile.uri) ?: return@withContext

                backupFileStream.use { IOUtils.copy(originalDatabaseFile.inputStream(), it) }

                outputFile = backupFile
            }
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
