package xyz.sevive.arcaeaoffline.ui.screens.database.playdata

import android.content.Context
import android.net.Uri
import android.text.format.Formatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.io.files.SystemFileSystem
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.api.ArcaeaResourcesApiClient
import xyz.sevive.arcaeaoffline.core.database.externals.exporters.ArcaeaOfflineDEFv2Exporter
import xyz.sevive.arcaeaoffline.core.database.externals.importers.ArcaeaSt3PlayResultImporter
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.data.imports.ImportLogEvent
import xyz.sevive.arcaeaoffline.data.imports.ImportLogManager
import xyz.sevive.arcaeaoffline.data.imports.ImportLogObject
import xyz.sevive.arcaeaoffline.data.imports.ImportTaskQueue
import xyz.sevive.arcaeaoffline.helpers.context.copyToCache
import xyz.sevive.arcaeaoffline.helpers.context.uriSizeIfTooLarge
import java.io.OutputStream
import kotlin.time.Duration.Companion.seconds

class DatabasePlayDataViewModel(
    private val playResultRepo: PlayResultRepository,
    private val importLogManager: ImportLogManager,
) : ViewModel() {
    companion object {
        private const val LOG_TAG = "DatabasePlayDataVM"

        private const val LOG_TAG_IMPORT_ST3 = "I-St3"
        private const val LOG_TAG_EXPORT_PLAY_RESULTS = "E-PR"
    }

    internal data class UiState(
        val isWorking: Boolean = false,
        val logs: List<ImportLogObject> = emptyList(),
    )

    private val logger = Logger.withTag(LOG_TAG)

    private val taskQueue =
        ImportTaskQueue(
            parentScope = viewModelScope,
            importLogManager = importLogManager,
            logger = logger,
        )

    internal val uiState =
        combine(
            taskQueue.isWorking,
            importLogManager.logs,
        ) { isWorking, logs ->
            UiState(
                isWorking = isWorking,
                logs = logs.sortedByDescending { it.timestamp },
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            UiState(),
        )

    private suspend fun sendTask(action: suspend CoroutineScope.() -> Unit) {
        taskQueue.send(action)
    }

    /**
     * Returns true when [uri] exceeds the import size cap, appending a log entry under [logTag];
     * the caller returns early on true.
     */
    private suspend fun Context.importFileTooLarge(
        uri: Uri,
        logTag: String,
    ): Boolean {
        val actualSize = uriSizeIfTooLarge(uri) ?: return false
        importLogManager.append(
            logTag,
            ImportLogEvent.Raw(
                "input file too large, limit is ${Formatter.formatFileSize(this, ArcaeaResourcesApiClient.DEFAULT_MAX_RESOURCE_BYTES)} " +
                    "while input is ${Formatter.formatFileSize(this, actualSize)}!",
            ),
        )
        return true
    }

    private suspend fun importSt3(conn: SQLiteConnection) {
        val playResults = ArcaeaSt3PlayResultImporter.playResults(conn)
        val affectedRows =
            playResultRepo.upsertBatch(*playResults.toTypedArray()).size

        importLogManager.append(
            LOG_TAG_IMPORT_ST3,
            ImportLogEvent.Plural(R.plurals.database_play_results_imported, affectedRows),
        )
    }

    fun importSt3(
        fileUri: Uri,
        context: Context,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                if (context.importFileTooLarge(fileUri, LOG_TAG_IMPORT_ST3)) return@sendTask

                val dbCacheFile = context.copyToCache(fileUri, "st3-import-temp") ?: return@sendTask

                BundledSQLiteDriver()
                    .open(dbCacheFile.toString(), SQLITE_OPEN_READONLY)
                    .use { conn -> importSt3(conn) }

                SystemFileSystem.delete(dbCacheFile)
            }
        }
    }

    private suspend fun exportPlayResults(outputStream: OutputStream) {
        val playResults = playResultRepo.findAll().firstOrNull() ?: return

        ArcaeaOfflineDEFv2Exporter.playResultsRoot(playResults).let {
            outputStream.write(ArcaeaOfflineDEFv2Exporter.playResults(it).toByteArray())
            importLogManager.append(
                LOG_TAG_EXPORT_PLAY_RESULTS,
                ImportLogEvent.Plural(R.plurals.database_play_results_exported, it.playResults.size),
            )
        }
    }

    fun exportPlayResults(
        uri: Uri,
        context: Context,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                context.contentResolver.openOutputStream(uri)?.use {
                    exportPlayResults(it)
                }
            }
        }
    }
}
