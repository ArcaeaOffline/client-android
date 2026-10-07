package xyz.sevive.arcaeaoffline.ui.activities

import android.content.Context
import android.text.format.Formatter
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.room.execSQL
import androidx.room.useWriterConnection
import androidx.work.WorkInfo
import androidx.work.WorkManager
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.path
import io.github.vinceglb.filekit.sink
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.database.OcrQueueDatabase
import xyz.sevive.arcaeaoffline.datastore.EmergencyModePreferencesRepository
import xyz.sevive.arcaeaoffline.jobs.ImageHashesDatabaseBuilderJob
import xyz.sevive.arcaeaoffline.jobs.OcrQueueProcessingJob
import xyz.sevive.arcaeaoffline.jobs.OcrQueueStagingJob
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class EmergencyModeActivityViewModel(
    context: Context,
    private val preferencesRepository: EmergencyModePreferencesRepository,
) : ViewModel() {
    companion object {
        private const val TEST_FILENAME = "arcaea_offline-test_write-1f8a11c6-65ce-4d73-886f-e0b5bc7f5eb9"
        private const val LOG_TAG = "EmergencyModeVM"

        /**
         * WorkManager unique names of every job that touches the OCR queue database.
         */
        private val ocrQueueDatabaseWorkNames =
            listOf(
                OcrQueueProcessingJob.WORK_NAME,
                OcrQueueStagingJob.WORK_NAME,
            )

        /**
         * WorkManager unique names of every job that reads or writes the OCR
         * dependency files.
         */
        private val ocrDependencyWorkNames =
            listOf(
                OcrQueueProcessingJob.WORK_NAME,
                ImageHashesDatabaseBuilderJob.NAME,
            )
    }

    private val logger = Logger.withTag(LOG_TAG)

    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)

    sealed interface DatabaseBackupState {
        data object Copying : DatabaseBackupState

        data class Success(
            val backupFileName: String,
            val backupFileSizeText: String,
        ) : DatabaseBackupState

        data class Failure(
            val message: String?,
        ) : DatabaseBackupState
    }

    private val _databaseBackupState = MutableStateFlow<DatabaseBackupState?>(null)
    val databaseBackupState = _databaseBackupState.asStateFlow()

    /**
     * Whether any [ocrQueueDatabaseWorkNames] job is currently running or waiting
     * to run. `true` before the first report just in case.
     */
    val isOcrQueueWorkRunning: StateFlow<Boolean> =
        workInfosFlow(ocrQueueDatabaseWorkNames)
            .map { infos ->
                infos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = 1000L),
                initialValue = true,
            )

    /**
     * Same as [isOcrQueueWorkRunning], but for jobs touching the OCR dependency files.
     */
    val isOcrDependenciesWorkRunning: StateFlow<Boolean> =
        workInfosFlow(ocrDependencyWorkNames)
            .map { infos ->
                infos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = 1000L),
                initialValue = true,
            )

    private fun workInfosFlow(workNames: List<String>) =
        workNames
            .map { workManager.getWorkInfosForUniqueWorkLiveData(it).asFlow() }
            .merge()

    val ocrDependencyFilesToDelete =
        OcrDependencyPaths().run {
            listOf(phashDatabaseFile, imageHashesDatabaseFile)
        }

    /** Filenames of every file deleted by [deleteAllOcrDependencies]. */
    val ocrDependencyFileNames: List<String>
        get() = ocrDependencyFilesToDelete.map { it.name }

    fun reloadPreferencesOnStartUp() {
        viewModelScope.launch {
            val preferences = preferencesRepository.preferencesFlow.firstOrNull() ?: return@launch
            val lastOutputDirectory = preferences.lastOutputDirectory ?: return@launch
            if (lastOutputDirectory.isEmpty()) return@launch

            val savedDir = PlatformFile(lastOutputDirectory)
            setOutputDirectory(savedDir)
        }
    }

    private val _outputDirectory = MutableStateFlow<PlatformFile?>(null)
    val outputDirectory = _outputDirectory.asStateFlow()

    /**
     * `null` while checking the directory's writability, otherwise checked result.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val outputDirectoryValid: StateFlow<Boolean?> =
        outputDirectory
            .flatMapLatest { directory ->
                flow {
                    if (directory == null) {
                        emit(false)
                    } else {
                        emit(null)
                        emit(outputDirectoryValidResultProducer(directory))
                    }
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = 1000L),
                initialValue = null,
            )

    fun setOutputDirectory(file: PlatformFile) {
        _outputDirectory.value = file

        viewModelScope.launch {
            preferencesRepository.updateLastOutputDirectory(file.path)
        }
    }

    private suspend fun outputDirectoryValidResultProducer(directory: PlatformFile): Boolean =
        try {
            withContext(Dispatchers.IO) {
                val testFile = PlatformFile(directory, TEST_FILENAME)
                testFile.write(ByteArray(0))
                testFile.delete()
            }

            true
        } catch (_: Exception) {
            false
        }

    sealed interface OcrDependenciesDeleteState {
        data object Deleted : OcrDependenciesDeleteState

        data class Failed(
            val message: String?,
        ) : OcrDependenciesDeleteState
    }

    private val _ocrDependenciesDeleteState = MutableStateFlow<OcrDependenciesDeleteState?>(null)
    val ocrDependenciesDeleteState = _ocrDependenciesDeleteState.asStateFlow()

    fun deleteAllOcrDependencies() {
        viewModelScope.launch(Dispatchers.IO) {
            val error =
                runCatching {
                    ocrDependencyWorkNames.forEach { workManager.cancelUniqueWork(it) }

                    // Wait until no tracked job is RUNNING/ENQUEUED, then give in-flight
                    // file accesses a moment to land.
                    withTimeoutOrNull(5.seconds) { isOcrDependenciesWorkRunning.first { !it } }
                    delay(500.milliseconds)

                    ocrDependencyFilesToDelete.forEach {
                        if (SystemFileSystem.exists(it)) SystemFileSystem.delete(it)
                    }
                    ocrDependencyFilesToDelete.forEach {
                        check(!SystemFileSystem.exists(it)) {
                            "dependency file still exists after deletion: ${it.name}"
                        }
                    }
                }.exceptionOrNull()

            _ocrDependenciesDeleteState.value =
                when (error) {
                    null -> {
                        OcrDependenciesDeleteState.Deleted
                    }

                    else -> {
                        logger.e(error) { "Error deleting OCR dependencies" }
                        OcrDependenciesDeleteState.Failed(error.message)
                    }
                }
        }
    }

    fun deleteOcrQueueDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val error =
                runCatching {
                    ocrQueueDatabaseWorkNames.forEach { workManager.cancelUniqueWork(it) }

                    // Wait until no tracked job is RUNNING/ENQUEUED, then give in-flight
                    // NonCancellable cleanups a moment to land.
                    withTimeoutOrNull(5.seconds) { isOcrQueueWorkRunning.first { !it } }
                    delay(500.milliseconds)

                    OcrQueueDatabase.getDatabase(appContext).close()

                    appContext.deleteDatabase(OcrQueueDatabase.DATABASE_FILENAME)
                    check(!appContext.getDatabasePath(OcrQueueDatabase.DATABASE_FILENAME).exists()) {
                        "database file still exists after deletion"
                    }
                }.exceptionOrNull()

            launch(Dispatchers.Main) {
                val message =
                    when (error) {
                        null -> appContext.getString(R.string.general_delete)
                        else -> error::class.simpleName ?: "ERROR"
                    }

                Toast.makeText(appContext, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun copyDatabase() {
        if (_databaseBackupState.value == DatabaseBackupState.Copying) return
        val outputDir = outputDirectory.value ?: return

        viewModelScope.launch {
            _databaseBackupState.value = DatabaseBackupState.Copying

            _databaseBackupState.value =
                withContext(Dispatchers.IO) {
                    try {
                        // Merge WAL into the main database file, so that copying it alone
                        // produces a complete backup.
                        val database = ArcaeaOfflineDatabase.getDatabase(appContext)
                        database.useWriterConnection { connection ->
                            connection.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
                        }

                        val originalDatabaseFile = appContext.getDatabasePath(ArcaeaOfflineDatabase.DATABASE_FILENAME)
                        val backupFileName = "arcaea_offline_${System.currentTimeMillis()}.db"
                        val backupFile = PlatformFile(outputDir, backupFileName)

                        originalDatabaseFile
                            .inputStream()
                            .asSource()
                            .buffered()
                            .use { fileSource ->
                                backupFile
                                    .sink(append = false)
                                    .buffered()
                                    .use { backupSink -> fileSource.transferTo(backupSink) }
                            }

                        val backupFileSizeText = Formatter.formatShortFileSize(appContext, backupFile.size())
                        DatabaseBackupState.Success(backupFile.name, backupFileSizeText)
                    } catch (e: Exception) {
                        logger.e(e) { "Error copying database" }
                        DatabaseBackupState.Failure(e.message)
                    }
                }
        }
    }
}
