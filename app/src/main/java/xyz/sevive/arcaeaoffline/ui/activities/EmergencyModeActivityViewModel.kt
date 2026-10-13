package xyz.sevive.arcaeaoffline.ui.activities

import android.content.Context
import android.text.format.Formatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room3.executeSQL
import androidx.room3.useWriterConnection
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.getAndUpdate
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
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueTaskDao
import xyz.sevive.arcaeaoffline.datastore.EmergencyModePreferencesRepository
import xyz.sevive.arcaeaoffline.helpers.diagnostics.DiagnosticsCollector
import xyz.sevive.arcaeaoffline.jobs.ImageHashesDatabaseBuilderJob
import xyz.sevive.arcaeaoffline.jobs.OcrQueueProcessingJob
import xyz.sevive.arcaeaoffline.jobs.OcrQueueStagingJob
import xyz.sevive.arcaeaoffline.ui.components.OperationState
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

sealed interface OutputDirectoryState {
    /** The chosen directory, or `null` when none is chosen. */
    val directory: PlatformFile?

    data object None : OutputDirectoryState {
        override val directory: PlatformFile? = null
    }

    data class Checking(
        override val directory: PlatformFile,
    ) : OutputDirectoryState

    data class Checked(
        override val directory: PlatformFile,
        val isValid: Boolean,
    ) : OutputDirectoryState
}

class EmergencyModeActivityViewModel(
    context: Context,
    private val preferencesRepository: EmergencyModePreferencesRepository,
    private val diagnosticsCollector: DiagnosticsCollector,
    private val ocrQueueTaskDao: OcrQueueTaskDao,
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

    private val _databaseBackupState = MutableStateFlow<OperationState?>(null)
    val databaseBackupState = _databaseBackupState.asStateFlow()

    private val _ocrDependenciesDeleteState = MutableStateFlow<OperationState?>(null)
    val ocrDependenciesDeleteState = _ocrDependenciesDeleteState.asStateFlow()

    private val _ocrQueueDeleteState = MutableStateFlow<OperationState?>(null)
    val ocrQueueDeleteState = _ocrQueueDeleteState.asStateFlow()

    private val _ocrQueueClearState = MutableStateFlow<OperationState?>(null)
    val ocrQueueClearState = _ocrQueueClearState.asStateFlow()

    val isOcrQueueWorkRunning: StateFlow<Boolean> = isWorkRunning(ocrQueueDatabaseWorkNames)

    val isOcrDependenciesWorkRunning: StateFlow<Boolean> = isWorkRunning(ocrDependencyWorkNames)

    /**
     * Whether any WorkManager job in [workNames] is currently RUNNING or ENQUEUED.
     */
    private fun isWorkRunning(workNames: List<String>): StateFlow<Boolean> =
        combine(workNames.map { workManager.getWorkInfosForUniqueWorkFlow(it) }) { workInfoLists ->
            workInfoLists
                .flatMap { it.toList() }
                .any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = 1000L),
            // use true to block destructive operations that rely on running state detection
            initialValue = true,
        )

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

    private val outputDirectory = MutableStateFlow<PlatformFile?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val outputDirectoryState: StateFlow<OutputDirectoryState> =
        outputDirectory
            .flatMapLatest { directory ->
                flow {
                    when (directory) {
                        null -> {
                            emit(OutputDirectoryState.None)
                        }

                        else -> {
                            emit(OutputDirectoryState.Checking(directory))
                            emit(
                                OutputDirectoryState.Checked(
                                    directory,
                                    outputDirectoryValidResultProducer(directory),
                                ),
                            )
                        }
                    }
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = 1000L),
                initialValue = OutputDirectoryState.None,
            )

    fun setOutputDirectory(file: PlatformFile) {
        outputDirectory.value = file

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

    /**
     * Launches a one-shot operation and updates its status in [state].
     *
     * Does nothing if [state] is already [OperationState.InProgress].
     *
     * Sets [state] to [OperationState.InProgress], then invokes [work] on [Dispatchers.IO].
     * - On success, [state] becomes [OperationState.Success] with the message returned by [work].
     * - On failure, [onError] is invoked and [state] becomes [OperationState.Failed] with the
     *   result of [failureMessage], or `null` if it returns null.
     *
     * The coroutine is scoped to [viewModelScope]; cancellation propagates to [work] and the
     * resulting [OperationState] is never set.
     */
    private fun launchOperation(
        state: MutableStateFlow<OperationState?>,
        onError: (Throwable) -> Unit = {},
        failureMessage: (Throwable) -> String? = { e ->
            buildString {
                e::class.simpleName?.let { append("$it: ") }
                append(e.message)
            }
        },
        work: suspend () -> String,
    ) {
        // atomic check & set progress state
        val previous = state.getAndUpdate { OperationState.InProgress }
        if (previous is OperationState.InProgress) return

        viewModelScope.launch {
            try {
                state.value =
                    withContext(Dispatchers.IO) {
                        try {
                            OperationState.Success(work())
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Throwable) {
                            onError(e)
                            OperationState.Failed(failureMessage(e))
                        }
                    }
            } finally {
                // reset progress if canceled mid-operation
                state.compareAndSet(OperationState.InProgress, null)
            }
        }
    }

    fun deleteAllOcrDependencies() {
        launchOperation(
            _ocrDependenciesDeleteState,
            onError = { logger.e(it) { "Error deleting OCR dependencies" } },
        ) {
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

            appContext.getString(R.string.emergency_mode_ocr_dependencies_deleted)
        }
    }

    fun clearOcrQueue() {
        launchOperation(
            _ocrQueueClearState,
            onError = { logger.e(it) { "Error clearing OCR queue" } },
        ) {
            ocrQueueDatabaseWorkNames.forEach { workManager.cancelUniqueWork(it) }

            // Wait until no tracked job is RUNNING/ENQUEUED, then give in-flight
            // database accesses a moment to land.
            withTimeoutOrNull(5.seconds) { isOcrQueueWorkRunning.first { !it } }
            delay(500.milliseconds)

            ocrQueueTaskDao.deleteAll()

            appContext.getString(R.string.general_action_done)
        }
    }

    fun deleteOcrQueueDatabase() {
        launchOperation(
            _ocrQueueDeleteState,
            onError = { logger.e(it) { "Error deleting OCR queue database" } },
        ) {
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

            // The Room singleton stays closed behind this, so the app
            // must restart for OCR features to work again.
            terminateApp()
            appContext.getString(R.string.emergency_mode_delete_ocr_queue_success)
        }
    }

    /**
     * Terminate app after a short delay.
     */
    private fun terminateApp() {
        viewModelScope.launch {
            withContext(NonCancellable) {
                // Give the result states a moment to be seen.
                delay(1.seconds)
                exitProcess(0)
            }
        }
    }

    private val _diagnosticsExportState = MutableStateFlow<OperationState?>(null)
    val diagnosticsExportState: StateFlow<OperationState?> = _diagnosticsExportState.asStateFlow()

    fun exportDiagnostics() {
        val outputDir = outputDirectory.value ?: return

        launchOperation(_diagnosticsExportState) {
            val target = diagnosticsCollector.exportTo(outputDir)
            appContext.getString(R.string.diagnostics_generated, target.name)
        }
    }

    fun copyDatabase() {
        val outputDir = outputDirectory.value ?: return

        launchOperation(
            _databaseBackupState,
            onError = { logger.e(it) { "Error copying database" } },
        ) {
            // Merge WAL into the main database file, so that copying it alone
            // produces a complete backup.
            val database = ArcaeaOfflineDatabase.getDatabase(appContext)
            database.useWriterConnection { connection ->
                connection.executeSQL("PRAGMA wal_checkpoint(TRUNCATE)")
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
            appContext.getString(
                R.string.emergency_mode_database_copied_message,
                backupFile.name,
                backupFileSizeText,
            )
        }
    }
}
