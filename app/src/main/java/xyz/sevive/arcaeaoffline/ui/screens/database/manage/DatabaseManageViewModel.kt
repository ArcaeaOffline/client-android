package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
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
import kotlinx.io.files.SystemFileSystem
import org.apache.commons.compress.archivers.zip.ZipFile
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
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

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

    private val importLogManager = ImportLogManager()

    internal data class UiState(
        val isWorking: Boolean = false,
        val logs: List<ImportLogObject> = emptyList(),
    )

    data class Task(
        val uuid: Uuid = Uuid.generateV4(),
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
                            importLogManager.append(
                                tag = null,
                                event = ImportLogEvent.Raw(e.toString()),
                            )
                        }
                    }.join()
                taskChannelActive.value = false
            }
        }
    }

    internal val uiState =
        combine(taskChannelActive, importLogManager.logs) { isWorking, logs ->
            UiState(isWorking = isWorking, logs = logs.sortedByDescending { it.timestamp })
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5.seconds.inWholeMilliseconds),
            UiState(),
        )

    private fun InputStream.readText(charset: Charset = StandardCharsets.UTF_8): String = bufferedReader(charset).use { it.readText() }

    private suspend fun sendTask(action: suspend CoroutineScope.() -> Unit) {
        Task(action = action).let {
            taskChannel.send(it)
            logger.d { "Task ${it.uuid} sent" }
        }
    }

    private suspend fun importPacklistTask(packlistContent: String) {
        val importer = ArcaeaPacklistImporter(packlistContent)

        val packs = importer.packs().toTypedArray()
        val packsAffectedRows = packRepo.upsertBatch(*packs).size
        logger.i { "$packsAffectedRows packs updated" }
        importLogManager.append(
            LOG_TAG_IMPORT_PACKLIST,
            ImportLogEvent.Plural(R.plurals.database_packs_imported, packsAffectedRows),
        )

        val packsLocalized = importer.packsLocalized()
        val packsLocalizedAffectedRows =
            packLocalizedRepo.insertBatch(packsLocalized).size
        logger.i { "$packsLocalizedAffectedRows packs localized updated" }
        importLogManager.append(
            LOG_TAG_IMPORT_PACKLIST,
            ImportLogEvent.Plural(R.plurals.database_packs_localized_imported, packsLocalizedAffectedRows),
        )
    }

    fun importPacklist(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val packlistContent = PlatformFile(uri).readBytes().decodeToString()
                importPacklistTask(packlistContent)
            }
        }
    }

    private suspend fun importSonglistTask(
        primarySonglistContent: String,
        supplementSonglistContent: String,
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
        importLogManager.append(
            LOG_TAG_IMPORT_SONGLIST,
            ImportLogEvent.Plural(R.plurals.database_songs_imported, songsAffectedRows),
        )

        val supplementDifficulties =
            supplementImporter
                .difficulties()
                .filter { it.songId in deletedSongIds }
        val difficulties = (importer.difficulties() + supplementDifficulties).toTypedArray()
        val difficultiesAffectedRows =
            difficultyRepo.upsertBatch(*difficulties).size
        logger.i { "$difficultiesAffectedRows difficulties updated" }
        importLogManager.append(
            LOG_TAG_IMPORT_SONGLIST,
            ImportLogEvent.Plural(R.plurals.database_difficulties_imported, difficultiesAffectedRows),
        )

        val supplementSongsLocalized =
            supplementImporter
                .songsLocalized()
                .filter { it.id in deletedSongIds }
        val songsLocalized = importer.songsLocalized() + supplementSongsLocalized
        val songsLocalizedAffectedRows =
            songLocalizedRepo.insertBatch(songsLocalized).size
        logger.i { "$songsLocalizedAffectedRows songs localized updated" }
        importLogManager.append(
            LOG_TAG_IMPORT_SONGLIST,
            ImportLogEvent.Plural(R.plurals.database_songs_localized_imported, songsLocalizedAffectedRows),
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
        importLogManager.append(
            LOG_TAG_IMPORT_SONGLIST,
            ImportLogEvent.Plural(R.plurals.database_difficulties_localized_imported, difficultiesLocalizedAffectedRows),
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
                importSonglistTask(songlistContent, supplementSonglistContent)
            }
        }
    }

    private suspend fun importArcaeaApkFromSelectedTask(
        zipFile: ZipFile,
        supplementSonglistContent: String,
    ) {
        zipFile.getEntry(ArcaeaPackageHelper.APK_PACKLIST_FILE_ENTRY_NAME)?.let { packlistEntry ->
            zipFile.getInputStream(packlistEntry).use {
                importPacklistTask(it.readText())
            }
        } ?: importLogManager.append(LOG_TAG_IMPORT_ARCAEA_APK, ImportLogEvent.Raw("packlist not found!"))

        zipFile.getEntry(ArcaeaPackageHelper.APK_SONGLIST_FILE_ENTRY_NAME)?.let { songlistEntry ->
            zipFile.getInputStream(songlistEntry).use {
                importSonglistTask(it.readText(), supplementSonglistContent)
            }
        } ?: importLogManager.append(LOG_TAG_IMPORT_ARCAEA_APK, ImportLogEvent.Raw("songlist not found!"))
    }

    fun importArcaeaApkFromSelected(
        uri: Uri,
        context: Context,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val supplementSonglistContent = context.assets.open("songlist.json").use { it.readText() }

                importLogManager.append(
                    LOG_TAG_IMPORT_ARCAEA_APK,
                    ImportLogEvent.SimpleString(R.string.database_manage_import_reading_apk),
                )

                context.contentResolver.openFileDescriptor(uri, "r").use { pfd ->
                    val fd = pfd?.fileDescriptor ?: return@sendTask

                    FileInputStream(fd).use { fis ->
                        val fileChannel = fis.channel

                        ZipFile.builder().setSeekableByteChannel(fileChannel).get().use { zipFile ->
                            importArcaeaApkFromSelectedTask(zipFile, supplementSonglistContent)
                        }
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
                val packageHelper = ArcaeaPackageHelper(context)

                packageHelper.getApkZipFile()?.use {
                    val packlistEntry =
                        it.getEntry(ArcaeaPackageHelper.APK_PACKLIST_FILE_ENTRY_NAME)
                    val songlistEntry =
                        it.getEntry(ArcaeaPackageHelper.APK_SONGLIST_FILE_ENTRY_NAME)

                    if (packlistEntry != null) {
                        val inputStream = it.getInputStream(packlistEntry)
                        importPacklistTask(inputStream.use { inputStream.readText() })
                    } else {
                        importLogManager.append(LOG_TAG_IMPORT_ARCAEA_INSTALLED, ImportLogEvent.Raw("packlist not found!"))
                    }

                    if (songlistEntry != null) {
                        val inputStream = it.getInputStream(songlistEntry)
                        importSonglistTask(inputStream.use { inputStream.readText() }, supplementSonglistContent)
                    } else {
                        importLogManager.append(LOG_TAG_IMPORT_ARCAEA_INSTALLED, ImportLogEvent.Raw("songlist not found!"))
                    }
                } ?: importLogManager.append(LOG_TAG_IMPORT_ARCAEA_INSTALLED, ImportLogEvent.Raw("apk zip file invalid!"))
            }
        }
    }

    private suspend fun importChartsInfoDatabase(conn: SQLiteConnection) {
        val chartInfo = ChartInfoDatabaseImporter.chartInfo(conn)
        val affectedRows =
            chartInfoRepo.insertBatch(*chartInfo.toTypedArray()).size
        logger.i { "$affectedRows chart info imported" }
        importLogManager.append(
            LOG_TAG_IMPORT_CHART_INFO_DATABASE,
            ImportLogEvent.Plural(R.plurals.database_chart_info_imported, affectedRows),
        )
    }

    fun importChartsInfoDatabase(
        fileUri: Uri,
        context: Context,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val databaseCopied =
                    context.copyToCache(fileUri, "chart_info_database_copy.db") ?: return@sendTask

                BundledSQLiteDriver()
                    .open(databaseCopied.toString(), SQLITE_OPEN_READONLY)
                    .use { conn -> importChartsInfoDatabase(conn) }

                SystemFileSystem.delete(databaseCopied)
            }
        }
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
