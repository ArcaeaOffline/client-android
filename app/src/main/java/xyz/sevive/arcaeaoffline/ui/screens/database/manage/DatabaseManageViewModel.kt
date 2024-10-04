package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.requery.android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.io.IOUtils
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.externals.exporters.ArcaeaOfflineDEFv2Exporter
import xyz.sevive.arcaeaoffline.core.database.externals.importers.ArcaeaPacklistImporter
import xyz.sevive.arcaeaoffline.core.database.externals.importers.ArcaeaSonglistImporter
import xyz.sevive.arcaeaoffline.core.database.externals.importers.ArcaeaSt3PlayResultImporter
import xyz.sevive.arcaeaoffline.core.database.externals.importers.ChartInfoDatabaseImporter
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPackageHelper
import xyz.sevive.arcaeaoffline.helpers.ArcaeaResourcesStateHelper
import xyz.sevive.arcaeaoffline.helpers.context.copyToCache
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.zip.ZipInputStream
import kotlin.time.Duration.Companion.seconds


class DatabaseManageViewModel(
    private val res: Resources,
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    companion object {
        private const val LOG_TAG = "DatabaseManageVM"

        private const val LOG_TAG_IMPORT_PACKLIST = "I-Pklst"
        private const val LOG_TAG_IMPORT_SONGLIST = "I-Slst"
        private const val LOG_TAG_IMPORT_ARCAEA_APK = "I-ArcApk"
        private const val LOG_TAG_IMPORT_ARCAEA_INSTALLED = "I-ArcLocal"
        private const val LOG_TAG_IMPORT_CHART_INFO_DATABASE = "I-CIDb"
        private const val LOG_TAG_IMPORT_ST3 = "I-St3"
        private const val LOG_TAG_EXPORT_PLAY_RESULTS = "E-PR"
    }

    data class LogObject(
        val uuid: UUID = UUID.randomUUID(),
        val timestamp: Instant,
        val tag: String? = null,
        val message: String,
    )

    data class UiState(
        val isWorking: Boolean = false,
        val logObjects: List<LogObject> = emptyList(),
    )

    data class Task(
        val uuid: UUID = UUID.randomUUID(),
        val action: suspend CoroutineScope.() -> Unit,
    )

    private val taskChannelActive = MutableStateFlow(false)
    private val taskScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val taskChannel = Channel<Task>(Channel.UNLIMITED)

    init {
        viewModelScope.launch(Dispatchers.Default) {
            taskChannel.consumeEach {
                taskChannelActive.value = true
                Log.d(LOG_TAG, "Processing task ${it.uuid}")
                taskScope.launch {
                    try {
                        it.action(this)
                    } catch (e: Throwable) {
                        Log.e(LOG_TAG, "Error processing task ${it.uuid}", e)
                        appendUiLog(tag = null, message = e.toString())
                    }
                }.join()
                taskChannelActive.value = false
            }
        }
    }

    private val logLock = Mutex()
    private val logs = MutableStateFlow(emptyList<LogObject>())

    val uiState = combine(taskChannelActive, logs) { isWorking, logs ->
        UiState(isWorking = isWorking, logObjects = logs.sortedByDescending { it.timestamp })
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
        UiState(),
    )

    private fun InputStream.readText(charset: Charset = StandardCharsets.UTF_8): String {
        return IOUtils.toString(this, charset)
    }

    private suspend fun appendUiLog(tag: String?, message: String) {
        logLock.withLock {
            logs.value += LogObject(timestamp = Instant.now(), tag = tag, message = message)
        }
    }

    private suspend fun sendTask(action: suspend CoroutineScope.() -> Unit) {
        Task(action = action).let {
            taskChannel.send(it)
            Log.d(LOG_TAG, "Task ${it.uuid} sent")
        }
    }

    private suspend fun importPacklistTask(packlistContent: String) {
        val importer = ArcaeaPacklistImporter(packlistContent)

        val packs = importer.packs().toTypedArray()
        val packsAffectedRows = repositoryContainer.packRepo.upsertBatch(*packs).size
        Log.i(LOG_TAG, "$packsAffectedRows packs updated")
        appendUiLog(
            LOG_TAG_IMPORT_PACKLIST,
            res.getQuantityString(
                R.plurals.database_packs_imported,
                packsAffectedRows,
                packsAffectedRows,
            )
        )

        val packsLocalized = importer.packsLocalized()
        val packsLocalizedAffectedRows =
            repositoryContainer.packLocalizedRepo.insertBatch(packsLocalized).size
        Log.i(LOG_TAG, "$packsLocalizedAffectedRows packs localized updated")
        appendUiLog(
            LOG_TAG_IMPORT_PACKLIST,
            res.getQuantityString(
                R.plurals.database_packs_localized_imported,
                packsLocalizedAffectedRows,
                packsLocalizedAffectedRows,
            )
        )
    }

    fun importPacklist(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    appendUiLog(
                        LOG_TAG_IMPORT_PACKLIST,
                        "Cannot open packlist inputStream from $uri, aborting!"
                    )
                    return@sendTask
                }
                val packlistContent = inputStream.use { inputStream.readText() }

                importPacklistTask(packlistContent)
            }
        }
    }

    private suspend fun importSonglistTask(songlistContent: String) {
        val importer = ArcaeaSonglistImporter(songlistContent)

        val songs = importer.songs().toTypedArray()
        val songsAffectedRows = repositoryContainer.songRepo.upsertBatch(*songs).size
        Log.i(LOG_TAG, "$songsAffectedRows songs updated")
        appendUiLog(
            LOG_TAG_IMPORT_SONGLIST,
            res.getQuantityString(
                R.plurals.database_songs_imported,
                songsAffectedRows,
                songsAffectedRows,
            )
        )

        val difficulties = importer.difficulties().toTypedArray()
        val difficultiesAffectedRows =
            repositoryContainer.difficultyRepo.upsertBatch(*difficulties).size
        Log.i(LOG_TAG, "$difficultiesAffectedRows difficulties updated")
        appendUiLog(
            LOG_TAG_IMPORT_SONGLIST,
            res.getQuantityString(
                R.plurals.database_difficulties_imported,
                difficultiesAffectedRows,
                difficultiesAffectedRows,
            )
        )

        val songsLocalized = importer.songsLocalized()
        val songsLocalizedAffectedRows =
            repositoryContainer.songLocalizedRepo.insertBatch(songsLocalized).size
        Log.i(LOG_TAG, "$songsLocalizedAffectedRows songs localized updated")
        appendUiLog(
            LOG_TAG_IMPORT_SONGLIST,
            res.getQuantityString(
                R.plurals.database_songs_localized_imported,
                songsLocalizedAffectedRows,
                songsLocalizedAffectedRows,
            )
        )

        val difficultiesLocalized = importer.difficultiesLocalized()
        val difficultiesLocalizedAffectedRows =
            repositoryContainer.difficultyLocalizedRepo.insertBatch(difficultiesLocalized).size
        Log.i(LOG_TAG, "$difficultiesLocalizedAffectedRows difficulties localized updated")
        appendUiLog(
            LOG_TAG_IMPORT_SONGLIST,
            res.getQuantityString(
                R.plurals.database_difficulties_localized_imported,
                difficultiesLocalizedAffectedRows,
                difficultiesLocalizedAffectedRows,
            )
        )
    }

    fun importSonglist(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    appendUiLog(
                        LOG_TAG_IMPORT_SONGLIST,
                        "Cannot open songlist inputStream from $uri, aborting!"
                    )
                    return@sendTask
                }
                val songlistContent = inputStream.use { inputStream.readText() }

                importSonglistTask(songlistContent)
            }
        }
    }

    private suspend fun importArcaeaApkFromSelectedTask(zipInputStream: ZipInputStream) {
        appendUiLog(
            LOG_TAG_IMPORT_ARCAEA_APK,
            res.getString(R.string.database_manage_import_reading_apk)
        )

        var entry = zipInputStream.nextEntry

        var packlistFound = false
        var songlistFound = false

        while (entry != null) {
            if (entry.name == ArcaeaPackageHelper.APK_PACKLIST_FILE_ENTRY_NAME) {
                packlistFound = true
                importPacklistTask(zipInputStream.readText())
            }

            if (entry.name == ArcaeaPackageHelper.APK_SONGLIST_FILE_ENTRY_NAME) {
                songlistFound = true
                importSonglistTask(zipInputStream.readText())
            }

            entry = zipInputStream.nextEntry
        }

        if (!packlistFound) appendUiLog(LOG_TAG_IMPORT_ARCAEA_APK, "packlist not found!")
        if (!songlistFound) appendUiLog(LOG_TAG_IMPORT_ARCAEA_APK, "songlist not found!")
    }

    fun importArcaeaApkFromSelected(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                context.contentResolver.openInputStream(uri)?.use { fis ->
                    ZipInputStream(fis).use { zis -> importArcaeaApkFromSelectedTask(zis) }
                }
            }
        }
    }

    val canImportLists = ArcaeaResourcesStateHelper.canImportLists

    fun importArcaeaApkFromInstalled(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val packageHelper = ArcaeaPackageHelper(context)

                packageHelper.getApkZipFile().use {
                    val packlistEntry = packageHelper.getPacklistEntry()
                    val songlistEntry = packageHelper.getSonglistEntry()

                    if (packlistEntry != null) {
                        val inputStream = it.getInputStream(packlistEntry)
                        importPacklistTask(inputStream.use { inputStream.readText() })
                    } else {
                        appendUiLog(LOG_TAG_IMPORT_ARCAEA_INSTALLED, "packlist not found!")
                    }

                    if (songlistEntry != null) {
                        val inputStream = it.getInputStream(songlistEntry)
                        importSonglistTask(inputStream.use { inputStream.readText() })
                    } else {
                        appendUiLog(LOG_TAG_IMPORT_ARCAEA_INSTALLED, "songlist not found!")
                    }
                }
            }
        }
    }

    private suspend fun importChartsInfoDatabase(db: SQLiteDatabase) {
        val chartInfo = ChartInfoDatabaseImporter.chartInfo(db)
        val affectedRows =
            repositoryContainer.chartInfoRepo.insertBatch(*chartInfo.toTypedArray()).size
        Log.i(LOG_TAG, "$affectedRows chart info imported")
        appendUiLog(
            LOG_TAG_IMPORT_CHART_INFO_DATABASE,
            res.getQuantityString(
                R.plurals.database_chart_info_imported,
                affectedRows,
                affectedRows,
            )
        )
    }

    fun importChartsInfoDatabase(fileUri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val databaseCopied =
                    context.copyToCache(fileUri, "chart_info_database_copy.db") ?: return@sendTask

                SQLiteDatabase.openDatabase(
                    databaseCopied.path, null, SQLiteDatabase.OPEN_READONLY
                ).use { importChartsInfoDatabase(it) }

                databaseCopied.delete()
            }
        }
    }

    private suspend fun importSt3(db: SQLiteDatabase) {
        val playResults = ArcaeaSt3PlayResultImporter.playResults(db)
        val affectedRows =
            repositoryContainer.playResultRepo.upsertBatch(*playResults.toTypedArray()).size

        appendUiLog(
            LOG_TAG_IMPORT_ST3,
            res.getQuantityString(
                R.plurals.database_play_results_imported,
                affectedRows,
                affectedRows,
            )
        )
    }

    fun importSt3(fileUri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val dbCacheFile = context.copyToCache(fileUri, "st3-import-temp") ?: return@sendTask

                SQLiteDatabase.openDatabase(
                    dbCacheFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY
                ).use { importSt3(it) }

                dbCacheFile.delete()
            }
        }
    }

    private suspend fun exportPlayResults(outputStream: OutputStream) {
        val playResults = repositoryContainer.playResultRepo.findAll().firstOrNull() ?: return

        ArcaeaOfflineDEFv2Exporter.playResultsRoot(playResults).let {
            IOUtils.write(ArcaeaOfflineDEFv2Exporter.playResults(it), outputStream)
            appendUiLog(
                LOG_TAG_EXPORT_PLAY_RESULTS,
                res.getQuantityString(
                    R.plurals.database_play_results_exported,
                    it.playResults.size,
                    it.playResults.size,
                )
            )
        }
    }

    fun exportPlayResults(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                context.contentResolver.openOutputStream(uri)?.use {
                    exportPlayResults(it)
                }
            }
        }
    }
}
