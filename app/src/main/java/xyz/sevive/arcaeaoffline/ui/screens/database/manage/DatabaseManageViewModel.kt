package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.source
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
import kotlinx.io.asInputStream
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.externals.exporters.ArcaeaOfflineDEFv2Exporter
import xyz.sevive.arcaeaoffline.core.database.externals.importers.ArcaeaPacklistImporter
import xyz.sevive.arcaeaoffline.core.database.externals.importers.ArcaeaSonglistImporter
import xyz.sevive.arcaeaoffline.core.database.externals.importers.ArcaeaSt3PlayResultImporter
import xyz.sevive.arcaeaoffline.core.database.externals.importers.ChartInfoDatabaseImporter
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyLocalizedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PackLocalizedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PackRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongLocalizedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPackageHelper
import xyz.sevive.arcaeaoffline.helpers.ArcaeaResourcesStateHolder
import xyz.sevive.arcaeaoffline.helpers.context.copyToCache
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.zip.ZipInputStream
import kotlin.time.Duration.Companion.seconds

class DatabaseManageViewModel(
    private val packRepo: PackRepository,
    private val packLocalizedRepo: PackLocalizedRepository,
    private val songRepo: SongRepository,
    private val difficultyRepo: DifficultyRepository,
    private val songLocalizedRepo: SongLocalizedRepository,
    private val difficultyLocalizedRepo: DifficultyLocalizedRepository,
    private val chartInfoRepo: ChartInfoRepository,
    private val playResultRepo: PlayResultRepository,
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

    private val logger = Logger.withTag(LOG_TAG)

    private val taskChannelActive = MutableStateFlow(false)
    private val taskScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val taskChannel = Channel<Task>(Channel.UNLIMITED)

    init {
        viewModelScope.launch(Dispatchers.Default) {
            taskChannel.consumeEach {
                taskChannelActive.value = true
                logger.d { "Processing task ${it.uuid}" }
                taskScope
                    .launch {
                        try {
                            it.action(this)
                        } catch (e: Throwable) {
                            logger.e(e) { "Error processing task ${it.uuid}" }
                            appendUiLog(tag = null, message = e.toString())
                        }
                    }.join()
                taskChannelActive.value = false
            }
        }
    }

    private val logLock = Mutex()
    private val logs = MutableStateFlow(emptyList<LogObject>())

    val uiState =
        combine(taskChannelActive, logs) { isWorking, logs ->
            UiState(isWorking = isWorking, logObjects = logs.sortedByDescending { it.timestamp })
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            UiState(),
        )

    private fun InputStream.readText(charset: Charset = StandardCharsets.UTF_8): String = bufferedReader(charset).use { it.readText() }

    private suspend fun appendUiLog(
        tag: String?,
        message: String,
    ) {
        logLock.withLock {
            logs.value += LogObject(timestamp = Instant.now(), tag = tag, message = message)
        }
    }

    private suspend fun sendTask(action: suspend CoroutineScope.() -> Unit) {
        Task(action = action).let {
            taskChannel.send(it)
            logger.d { "Task ${it.uuid} sent" }
        }
    }

    private suspend fun importPacklistTask(
        packlistContent: String,
        resources: Resources,
    ) {
        val importer = ArcaeaPacklistImporter(packlistContent)

        val packs = importer.packs().toTypedArray()
        val packsAffectedRows = packRepo.upsertBatch(*packs).size
        logger.i { "$packsAffectedRows packs updated" }
        appendUiLog(
            LOG_TAG_IMPORT_PACKLIST,
            resources.getQuantityString(
                R.plurals.database_packs_imported,
                packsAffectedRows,
                packsAffectedRows,
            ),
        )

        val packsLocalized = importer.packsLocalized()
        val packsLocalizedAffectedRows =
            packLocalizedRepo.insertBatch(packsLocalized).size
        logger.i { "$packsLocalizedAffectedRows packs localized updated" }
        appendUiLog(
            LOG_TAG_IMPORT_PACKLIST,
            resources.getQuantityString(
                R.plurals.database_packs_localized_imported,
                packsLocalizedAffectedRows,
                packsLocalizedAffectedRows,
            ),
        )
    }

    fun importPacklist(
        uri: Uri,
        context: Context,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val packlistContent = PlatformFile(uri).readBytes().decodeToString()
                val resources = context.resources
                importPacklistTask(packlistContent, resources)
            }
        }
    }

    private suspend fun importSonglistTask(
        primarySonglistContent: String,
        supplementSonglistContent: String,
        resources: Resources,
    ) {
        val supplementImporter = ArcaeaSonglistImporter(supplementSonglistContent)
        val importer = ArcaeaSonglistImporter(primarySonglistContent)

        val deletedSongIds = importer.deletedSongIds()
        val supplementSongs =
            supplementImporter
                .songs()
                .filter { it.id in deletedSongIds }
                .map { it.copy(deletedInGame = true) }

        val songs = (importer.songs() + supplementSongs).toTypedArray()
        val songsAffectedRows = songRepo.upsertBatch(*songs).size
        logger.i { "$songsAffectedRows songs updated" }
        appendUiLog(
            LOG_TAG_IMPORT_SONGLIST,
            resources.getQuantityString(
                R.plurals.database_songs_imported,
                songsAffectedRows,
                songsAffectedRows,
            ),
        )

        val supplementDifficulties =
            supplementImporter
                .difficulties()
                .filter { it.songId in deletedSongIds }
        val difficulties = (importer.difficulties() + supplementDifficulties).toTypedArray()
        val difficultiesAffectedRows =
            difficultyRepo.upsertBatch(*difficulties).size
        logger.i { "$difficultiesAffectedRows difficulties updated" }
        appendUiLog(
            LOG_TAG_IMPORT_SONGLIST,
            resources.getQuantityString(
                R.plurals.database_difficulties_imported,
                difficultiesAffectedRows,
                difficultiesAffectedRows,
            ),
        )

        val supplementSongsLocalized =
            supplementImporter
                .songsLocalized()
                .filter { it.id in deletedSongIds }
        val songsLocalized = importer.songsLocalized() + supplementSongsLocalized
        val songsLocalizedAffectedRows =
            songLocalizedRepo.insertBatch(songsLocalized).size
        logger.i { "$songsLocalizedAffectedRows songs localized updated" }
        appendUiLog(
            LOG_TAG_IMPORT_SONGLIST,
            resources.getQuantityString(
                R.plurals.database_songs_localized_imported,
                songsLocalizedAffectedRows,
                songsLocalizedAffectedRows,
            ),
        )

        val supplementDifficultiesLocalized =
            supplementImporter
                .difficultiesLocalized()
                .filter { it.songId in deletedSongIds }
        val difficultiesLocalized =
            importer.difficultiesLocalized() + supplementDifficultiesLocalized
        val difficultiesLocalizedAffectedRows =
            difficultyLocalizedRepo.insertBatch(difficultiesLocalized).size
        logger.i { "$difficultiesLocalizedAffectedRows difficulties localized updated" }
        appendUiLog(
            LOG_TAG_IMPORT_SONGLIST,
            resources.getQuantityString(
                R.plurals.database_difficulties_localized_imported,
                difficultiesLocalizedAffectedRows,
                difficultiesLocalizedAffectedRows,
            ),
        )
    }

    fun importSonglist(
        uri: Uri,
        context: Context,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val songlistContent = PlatformFile(uri).readBytes().decodeToString()
                val supplementSonglistContent = context.assets.open("songlist.json").use { it.readText() }
                val resources = context.resources
                importSonglistTask(songlistContent, supplementSonglistContent, resources)
            }
        }
    }

    private suspend fun importArcaeaApkFromSelectedTask(
        zipInputStream: ZipInputStream,
        supplementSonglistContent: String,
        resources: Resources,
    ) {
        appendUiLog(
            LOG_TAG_IMPORT_ARCAEA_APK,
            resources.getString(R.string.database_manage_import_reading_apk),
        )

        var entry = zipInputStream.nextEntry

        var packlistFound = false
        var songlistFound = false

        while (entry != null) {
            if (entry.name == ArcaeaPackageHelper.APK_PACKLIST_FILE_ENTRY_NAME) {
                packlistFound = true
                importPacklistTask(zipInputStream.readText(), resources)
            }

            if (entry.name == ArcaeaPackageHelper.APK_SONGLIST_FILE_ENTRY_NAME) {
                songlistFound = true
                importSonglistTask(zipInputStream.readText(), supplementSonglistContent, resources)
            }

            entry = zipInputStream.nextEntry
        }

        if (!packlistFound) appendUiLog(LOG_TAG_IMPORT_ARCAEA_APK, "packlist not found!")
        if (!songlistFound) appendUiLog(LOG_TAG_IMPORT_ARCAEA_APK, "songlist not found!")
    }

    fun importArcaeaApkFromSelected(
        uri: Uri,
        context: Context,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val supplementSonglistContent = context.assets.open("songlist.json").use { it.readText() }
                val resources = context.resources
                PlatformFile(uri).source().buffered().asInputStream().use { inputStream ->
                    ZipInputStream(inputStream).use { zis ->
                        // TODO: change to work manager task, weird `java.io.IOException: Stream closed` inspected
                        importArcaeaApkFromSelectedTask(zis, supplementSonglistContent, resources)
                    }
                }
            }
        }
    }

    val canImportLists = ArcaeaResourcesStateHolder.canImportLists

    fun importArcaeaApkFromInstalled(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val supplementSonglistContent = context.assets.open("songlist.json").use { it.readText() }
                val resources = context.resources
                val packageHelper = ArcaeaPackageHelper(context)

                packageHelper.getApkZipFile()?.use {
                    val packlistEntry =
                        it.getEntry(ArcaeaPackageHelper.APK_PACKLIST_FILE_ENTRY_NAME)
                    val songlistEntry =
                        it.getEntry(ArcaeaPackageHelper.APK_SONGLIST_FILE_ENTRY_NAME)

                    if (packlistEntry != null) {
                        val inputStream = it.getInputStream(packlistEntry)
                        importPacklistTask(inputStream.use { inputStream.readText() }, resources)
                    } else {
                        appendUiLog(LOG_TAG_IMPORT_ARCAEA_INSTALLED, "packlist not found!")
                    }

                    if (songlistEntry != null) {
                        val inputStream = it.getInputStream(songlistEntry)
                        importSonglistTask(inputStream.use { inputStream.readText() }, supplementSonglistContent, resources)
                    } else {
                        appendUiLog(LOG_TAG_IMPORT_ARCAEA_INSTALLED, "songlist not found!")
                    }
                } ?: appendUiLog(LOG_TAG_IMPORT_ARCAEA_INSTALLED, "apk zip file invalid!")
            }
        }
    }

    private suspend fun importChartsInfoDatabase(
        conn: SQLiteConnection,
        resources: Resources,
    ) {
        val chartInfo = ChartInfoDatabaseImporter.chartInfo(conn)
        val affectedRows =
            chartInfoRepo.insertBatch(*chartInfo.toTypedArray()).size
        logger.i { "$affectedRows chart info imported" }
        appendUiLog(
            LOG_TAG_IMPORT_CHART_INFO_DATABASE,
            resources.getQuantityString(
                R.plurals.database_chart_info_imported,
                affectedRows,
                affectedRows,
            ),
        )
    }

    fun importChartsInfoDatabase(
        fileUri: Uri,
        context: Context,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val resources = context.resources
                val databaseCopied =
                    context.copyToCache(fileUri, "chart_info_database_copy.db") ?: return@sendTask

                BundledSQLiteDriver()
                    .open(databaseCopied.toString(), SQLITE_OPEN_READONLY)
                    .use { conn -> importChartsInfoDatabase(conn, resources) }

                SystemFileSystem.delete(databaseCopied)
            }
        }
    }

    private suspend fun importSt3(
        conn: SQLiteConnection,
        resources: Resources,
    ) {
        val playResults = ArcaeaSt3PlayResultImporter.playResults(conn)
        val affectedRows =
            playResultRepo.upsertBatch(*playResults.toTypedArray()).size

        appendUiLog(
            LOG_TAG_IMPORT_ST3,
            resources.getQuantityString(
                R.plurals.database_play_results_imported,
                affectedRows,
                affectedRows,
            ),
        )
    }

    fun importSt3(
        fileUri: Uri,
        context: Context,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val resources = context.resources
                val dbCacheFile = context.copyToCache(fileUri, "st3-import-temp") ?: return@sendTask

                BundledSQLiteDriver()
                    .open(dbCacheFile.toString(), SQLITE_OPEN_READONLY)
                    .use { conn -> importSt3(conn, resources) }

                SystemFileSystem.delete(dbCacheFile)
            }
        }
    }

    private suspend fun exportPlayResults(
        outputStream: OutputStream,
        resources: Resources,
    ) {
        val playResults = playResultRepo.findAll().firstOrNull() ?: return

        ArcaeaOfflineDEFv2Exporter.playResultsRoot(playResults).let {
            outputStream.write(ArcaeaOfflineDEFv2Exporter.playResults(it).toByteArray())
            appendUiLog(
                LOG_TAG_EXPORT_PLAY_RESULTS,
                resources.getQuantityString(
                    R.plurals.database_play_results_exported,
                    it.playResults.size,
                    it.playResults.size,
                ),
            )
        }
    }

    fun exportPlayResults(
        uri: Uri,
        context: Context,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val resources = context.resources
                context.contentResolver.openOutputStream(uri)?.use {
                    exportPlayResults(it, resources)
                }
            }
        }
    }
}
