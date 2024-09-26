package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import androidx.core.database.getIntOrNull
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.io.IOUtils
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.export.ArcaeaOfflineExportScore
import xyz.sevive.arcaeaoffline.core.database.externals.arcaea.PacklistParser
import xyz.sevive.arcaeaoffline.core.database.externals.arcaea.SonglistParser
import xyz.sevive.arcaeaoffline.core.database.externals.arcaea.importers.St3PlayResultImporter
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPackageHelper
import xyz.sevive.arcaeaoffline.helpers.GlobalArcaeaButtonStateHelper
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
    data class LogObject(
        val uuid: UUID = UUID.randomUUID(),
        val timestamp: Instant,
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
                        appendLog(e.toString())
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

    private fun InputStream.convertToString(charset: Charset = StandardCharsets.UTF_8): String {
        return IOUtils.toString(this, charset)
    }

    private suspend fun appendLog(message: String) {
        logLock.withLock {
            logs.value += LogObject(timestamp = Instant.now(), message = message)
        }
    }

    private suspend fun sendTask(action: suspend CoroutineScope.() -> Unit) {
        Task(action = action).let {
            taskChannel.send(it)
            Log.d(LOG_TAG, "Task ${it.uuid} sent")
        }
    }

    private suspend fun importPacklistTask(packlistContent: String) {
        val packs = PacklistParser(packlistContent).parsePack().toTypedArray()
        val affectedRows = repositoryContainer.packRepo.upsertAll(*packs).size

        Log.i(LOG_TAG, "$affectedRows packs updated")
        appendLog(
            res.getQuantityString(
                R.plurals.database_packlist_imported,
                affectedRows,
                affectedRows,
            )
        )
    }

    fun importPacklist(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    appendLog("Cannot open packlist inputStream from $uri, aborting!")
                    return@sendTask
                }
                val packlistContent = inputStream.use { inputStream.convertToString() }

                importPacklistTask(packlistContent)
            }
        }
    }

    private suspend fun importSonglistTask(songlistContent: String) {
        val songs = SonglistParser(songlistContent).parseSong().toTypedArray()
        val difficulties = SonglistParser(songlistContent).parseDifficulty().toTypedArray()

        val songsAffectedRows = repositoryContainer.songRepo.upsertAll(*songs).size
        val difficultiesAffectedRows =
            repositoryContainer.difficultyRepo.upsertAll(*difficulties).size

        Log.i(LOG_TAG, "$songsAffectedRows songs updated")
        Log.i(LOG_TAG, "$difficultiesAffectedRows difficulties updated")

        appendLog(
            res.getQuantityString(
                R.plurals.database_songlist_song_imported,
                songsAffectedRows,
                songsAffectedRows,
            )
        )
        appendLog(
            res.getQuantityString(
                R.plurals.database_songlist_difficulty_imported,
                difficultiesAffectedRows,
                difficultiesAffectedRows,
            )
        )
    }

    fun importSonglist(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    appendLog("Cannot open songlist inputStream from $uri, aborting!")
                    return@sendTask
                }
                val songlistContent = inputStream.use { inputStream.convertToString() }

                importSonglistTask(songlistContent)
            }
        }
    }

    private suspend fun importArcaeaApkFromSelectedTask(zipInputStream: ZipInputStream) {
        appendLog(res.getString(R.string.database_manage_import_reading_apk))

        var entry = zipInputStream.nextEntry

        var packlistFound = false
        var songlistFound = false

        while (entry != null) {
            if (entry.name == ArcaeaPackageHelper.APK_PACKLIST_FILE_ENTRY_NAME) {
                packlistFound = true
                importPacklistTask(zipInputStream.use { zipInputStream.convertToString() })
            }

            if (entry.name == ArcaeaPackageHelper.APK_SONGLIST_FILE_ENTRY_NAME) {
                songlistFound = true
                importSonglistTask(zipInputStream.use { zipInputStream.convertToString() })
            }

            entry = zipInputStream.nextEntry
        }

        if (!packlistFound) appendLog("packlist not found!")
        if (!songlistFound) appendLog("songlist not found!")
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

    val importArcaeaApkFromInstalledButtonState =
        GlobalArcaeaButtonStateHelper.importListsFromApkButtonState

    fun importArcaeaApkFromInstalled(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            sendTask {
                val packageHelper = ArcaeaPackageHelper(context)

                packageHelper.getApkZipFile().use {
                    val packlistEntry = packageHelper.getPacklistEntry()
                    val songlistEntry = packageHelper.getSonglistEntry()

                    if (packlistEntry != null) {
                        val inputStream = it.getInputStream(packlistEntry)
                        importPacklistTask(inputStream.use { inputStream.convertToString() })
                    } else {
                        appendLog("packlist not found!")
                    }

                    if (songlistEntry != null) {
                        val inputStream = it.getInputStream(songlistEntry)
                        importSonglistTask(inputStream.use { inputStream.convertToString() })
                    } else {
                        appendLog("songlist not found!")
                    }
                }
            }
        }
    }

    private suspend fun importChartsInfoDatabase(db: SQLiteDatabase) {
        val chartInfoList = mutableListOf<ChartInfo>()

        val cursor = db.query(
            "charts_info",
            arrayOf("song_id", "rating_class", "constant", "notes"),
            null,
            null,
            null,
            null,
            null,
        )

        cursor.moveToFirst()
        cursor.use {
            val songIdColumnIndex = it.getColumnIndex("song_id")
            val ratingClassColumnIndex = it.getColumnIndex("rating_class")
            val constantColumnIndex = it.getColumnIndex("constant")
            try {
                assert(songIdColumnIndex >= 0)
                assert(ratingClassColumnIndex >= 0)
                assert(constantColumnIndex >= 0)
            } catch (e: AssertionError) {
                appendLog("Database invalid!")
            }

            do {
                val songId = it.getString(songIdColumnIndex)
                val ratingClass = it.getInt(ratingClassColumnIndex)
                val constant = it.getInt(constantColumnIndex)
                val notes = it.getIntOrNull(it.getColumnIndex("notes"))

                val chartInfo = ChartInfo(
                    songId = songId,
                    ratingClass = ArcaeaRatingClass.fromInt(ratingClass),
                    constant = constant,
                    notes = notes
                )
                chartInfoList.add(chartInfo)
            } while (it.moveToNext())
        }

        val affectedRows = repositoryContainer.chartInfoRepo.insertAll(
            *chartInfoList.toTypedArray()
        ).size

        Log.i(LOG_TAG, "$affectedRows chart info imported")
        appendLog(
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
        val playResults = St3PlayResultImporter.playResults(db)
        val affectedRows =
            repositoryContainer.playResultRepo.upsertAll(*playResults.toTypedArray()).size

        appendLog(
            res.getQuantityString(
                R.plurals.database_play_result_imported,
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
        val writer = ArcaeaOfflineExportScore(repositoryContainer.playResultRepo)
        writer.toJsonObject()?.let {
            IOUtils.write(writer.toJsonString(it), outputStream)
            appendLog(
                res.getQuantityString(
                    R.plurals.database_play_result_exported,
                    it.scores.size,
                    it.scores.size,
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

    companion object {
        const val LOG_TAG = "DatabaseManageVM"
    }
}
