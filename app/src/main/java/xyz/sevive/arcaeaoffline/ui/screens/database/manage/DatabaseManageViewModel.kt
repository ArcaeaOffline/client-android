package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.database.getIntOrNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.requery.android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
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
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream


class DatabaseManageViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    private val _messages = MutableStateFlow<List<String>>(listOf())
    val messages = _messages.asStateFlow()

    private val _actionRunning = MutableStateFlow(false)
    val actionRunning = _actionRunning.asStateFlow()

    private fun actionStart() {
        _actionRunning.value = true
        _messages.value = listOf()
    }

    private fun actionEnd() {
        _actionRunning.value = false
    }

    private class ActionScope(private val messages: MutableStateFlow<List<String>>) {
        fun appendMessage(message: String) {
            val newMessages = messages.value.toMutableList()
            newMessages.add(message)
            messages.value = newMessages
        }
    }

    private suspend fun withAction(block: suspend ActionScope. () -> Unit) {
        val anotherActionRunning = actionRunning.value
        if (!anotherActionRunning) actionStart()
        block(ActionScope(_messages))
        if (!anotherActionRunning) actionEnd()
    }


    private suspend fun importPacklist(packlistContent: String, context: Context? = null) {
        withAction {
            val packs = PacklistParser(packlistContent).parsePack().toTypedArray()
            val affectedRows = repositoryContainer.packRepo.upsertAll(*packs)
            Log.i(LOG_TAG, "${affectedRows.size} packs updated")

            context?.let {
                appendMessage(
                    context.resources.getQuantityString(
                        R.plurals.database_packlist_imported, affectedRows.size, affectedRows.size
                    )
                )
            }
        }
    }

    suspend fun importPacklist(uri: Uri, context: Context) {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return
        importPacklist(IOUtils.toString(inputStream, StandardCharsets.UTF_8), context)
    }

    private suspend fun importSonglist(songlistContent: String, context: Context? = null) {
        withAction {
            val songs = SonglistParser(songlistContent).parseSong().toTypedArray()
            val difficulties = SonglistParser(songlistContent).parseDifficulty().toTypedArray()

            val songsAffected = repositoryContainer.songRepo.upsertAll(*songs)
            val difficultiesAffected = repositoryContainer.difficultyRepo.upsertAll(*difficulties)

            context?.let {
                val songText = context.resources.getQuantityString(
                    R.plurals.database_songlist_song_imported,
                    songsAffected.size,
                    songsAffected.size
                )
                appendMessage(songText)

                val difficultyText = context.resources.getQuantityString(
                    R.plurals.database_songlist_difficulty_imported,
                    difficultiesAffected.size,
                    difficultiesAffected.size
                )
                appendMessage(difficultyText)
            }

            Log.i(LOG_TAG, "${songsAffected.size} songs updated")
            Log.i(LOG_TAG, "${difficultiesAffected.size} difficulties updated")
        }
    }

    suspend fun importSonglist(uri: Uri, context: Context) {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return
        importSonglist(IOUtils.toString(inputStream, StandardCharsets.UTF_8), context)
    }

    private suspend fun importArcaeaApkFromZipInputStream(
        zipInputStream: ZipInputStream, context: Context? = null
    ) {
        withAction {
            context?.let {
                appendMessage(
                    context.resources.getString(R.string.database_manage_import_reading_apk)
                )
            }

            var entry = zipInputStream.nextEntry

            var packlistFound = false
            var songlistFound = false
            withContext(Dispatchers.IO) {
                while (entry != null) {
                    if (entry.name == ArcaeaPackageHelper.APK_PACKLIST_FILE_ENTRY_NAME) {
                        packlistFound = true
                        val buffer = ByteArrayOutputStream()
                        IOUtils.copy(zipInputStream, buffer)
                        importPacklist(
                            packlistContent = buffer.toByteArray().toString(StandardCharsets.UTF_8),
                            context = context,
                        )
                    }

                    if (entry.name == ArcaeaPackageHelper.APK_SONGLIST_FILE_ENTRY_NAME) {
                        songlistFound = true
                        val buffer = ByteArrayOutputStream()
                        IOUtils.copy(zipInputStream, buffer)
                        importSonglist(
                            songlistContent = buffer.toByteArray().toString(StandardCharsets.UTF_8),
                            context = context,
                        )
                    }

                    entry = zipInputStream.nextEntry
                }
            }

            context?.let {
                if (!packlistFound) appendMessage("packlist not found!")
                if (!songlistFound) appendMessage("songlist not found!")
            }
        }
    }

    suspend fun importArcaeaApkFromInputStream(inputStream: InputStream, context: Context) {
        ZipInputStream(inputStream).use {
            importArcaeaApkFromZipInputStream(it, context)
        }
    }

    val importArcaeaApkFromInstalledButtonState =
        GlobalArcaeaButtonStateHelper.importListsFromApkButtonState

    suspend fun importArcaeaApkFromInstalled(context: Context) {
        val packageHelper = ArcaeaPackageHelper(context)
        withAction {
            packageHelper.getApkZipFile().use {
                val packlistEntry = packageHelper.getPacklistEntry()
                val songlistEntry = packageHelper.getSonglistEntry()

                if (packlistEntry != null) {
                    val packlistInputStream = it.getInputStream(packlistEntry)
                    importPacklist(
                        IOUtils.toString(packlistInputStream, StandardCharsets.UTF_8), context
                    )
                } else {
                    delay(100L)  // ensuring a recomposition, same for below
                    appendMessage("packlist not found!")
                }

                if (songlistEntry != null) {
                    val songlistInputStream = it.getInputStream(songlistEntry)
                    importSonglist(
                        IOUtils.toString(songlistInputStream, StandardCharsets.UTF_8), context
                    )
                } else {
                    delay(100L)
                    appendMessage("songlist not found!")
                }
            }
        }
    }

    suspend fun importChartsInfoDatabase(fileUri: Uri, context: Context) {
        withAction {
            val databaseCopied =
                context.copyToCache(fileUri, "chart_info_database_copy.db") ?: return@withAction

            val chartInfoList = mutableListOf<ChartInfo>()
            SQLiteDatabase.openDatabase(
                databaseCopied.path, null, SQLiteDatabase.OPEN_READONLY
            ).use { db ->
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
                        appendMessage("Database invalid!")
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
            }

            val affectedRows = repositoryContainer.chartInfoRepo.insertAll(
                *chartInfoList.toTypedArray()
            )

            appendMessage(
                context.resources.getString(
                    R.string.database_chart_info_imported,
                    affectedRows.size,
                )
            )

            databaseCopied.delete()
        }
    }

    private suspend fun importSt3Suspend(fileUri: Uri, context: Context): Int? {
        val dbCacheFile = context.copyToCache(fileUri, "st3-import-temp") ?: return null

        val db = SQLiteDatabase.openDatabase(
            dbCacheFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY
        )
        val playResults = db.use { St3PlayResultImporter.playResults(it) }

        repositoryContainer.playResultRepo.upsertAll(*playResults.toTypedArray())
        return playResults.size
    }

    fun importSt3(fileUri: Uri, context: Context) {
        viewModelScope.launch {
            withAction {
                val count = importSt3Suspend(fileUri, context)

                delay(100L)

                if (count != null) appendMessage(
                    context.resources.getQuantityString(
                        R.plurals.database_play_result_imported, count, count
                    )
                )
                else appendMessage(context.resources.getString(R.string.general_unknown_error))
            }
        }
    }

    suspend fun exportScores(outputStream: OutputStream) {
        val content = ArcaeaOfflineExportScore(repositoryContainer.playResultRepo).toJsonString()
        content?.let {
            IOUtils.write(content, outputStream)
        }
    }

    companion object {
        const val LOG_TAG = "DatabaseManageViewModel"
    }
}
