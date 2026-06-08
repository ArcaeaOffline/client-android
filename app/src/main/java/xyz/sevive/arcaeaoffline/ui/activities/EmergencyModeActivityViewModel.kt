package xyz.sevive.arcaeaoffline.ui.activities

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.format.Formatter
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.SystemFileSystem
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.database.OcrQueueDatabase
import xyz.sevive.arcaeaoffline.datastore.EmergencyModePreferencesRepository

class EmergencyModeActivityViewModel(
    private val preferencesRepository: EmergencyModePreferencesRepository,
) : ViewModel() {
    companion object {
        private const val TEST_FILENAME = "arcaea_offline-test_write-1f8a11c6-65ce-4d73-886f-e0b5bc7f5eb9"
        private const val LOG_TAG = "EmergencyModeVM"
    }

    private val logger = Logger.withTag(LOG_TAG)

    fun reloadPreferencesOnStartUp() {
        viewModelScope.launch {
            val preferences = preferencesRepository.preferencesFlow.firstOrNull() ?: return@launch

            if (preferences.hasLastOutputDirectory()) {
                val lastOutputDirectory = preferences.lastOutputDirectory
                if (lastOutputDirectory.isNotEmpty()) {
                    val savedDir = PlatformFile(lastOutputDirectory)
                    setOutputDirectory(savedDir)
                }
            }
        }
    }

    private val _outputDirectory = MutableStateFlow<PlatformFile?>(null)
    val outputDirectory = _outputDirectory.asStateFlow()

    val outputDirectoryValid =
        outputDirectory.map { outputDirectoryValidResultProducer(it) }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = 1000L),
            initialValue = false,
        )

    fun setOutputDirectory(file: PlatformFile) {
        _outputDirectory.value = file

        viewModelScope.launch {
            preferencesRepository.updateLastOutputDirectory(file.path)
        }
    }

    suspend fun outputDirectoryValidResultProducer(directory: PlatformFile?): Boolean {
        if (directory == null) return false

        return try {
            withContext(Dispatchers.IO) {
                val testFile = PlatformFile(directory, TEST_FILENAME)
                testFile.write(ByteArray(0))
                testFile.delete()
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    fun deleteAllOcrDependencies() {
        val paths = OcrDependencyPaths()
        SystemFileSystem.delete(paths.knnModelFile)
        SystemFileSystem.delete(paths.phashDatabaseFile)
        SystemFileSystem.delete(paths.imageHashesDatabaseFile)
    }

    fun deleteOcrQueueDatabase(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                context.deleteDatabase(OcrQueueDatabase.DATABASE_FILENAME)
            }.invokeOnCompletion {
                launch(Dispatchers.Main) {
                    val message =
                        if (it == null) {
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
        var toastMessage: String?

        viewModelScope
            .launch(Dispatchers.IO) {
                val backupFileName = "arcaea_offline_${System.currentTimeMillis()}.db"
                val outputDir = outputDirectory.value ?: return@launch
                val backupFile = PlatformFile(outputDir, backupFileName)

                try {
                    backupFile.write(originalDatabaseFile.inputStream().use { it.readBytes() })

                    val fileSizeReadable = Formatter.formatShortFileSize(context, backupFile.size())
                    toastMessage =
                        context.getString(
                            R.string.emergency_mode_database_copied_message,
                            backupFile.name,
                            fileSizeReadable,
                        )
                } catch (e: Exception) {
                    logger.e(e) { "Error copying database" }
                    toastMessage = e.message ?: "Error copying database"
                }

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}
