package xyz.sevive.arcaeaoffline.ui.database.manage

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.database.getIntOrNull
import androidx.lifecycle.ViewModel
import io.requery.android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.export.ArcaeaOfflineExportScore
import xyz.sevive.arcaeaoffline.core.database.externals.arcaea.PacklistParser
import xyz.sevive.arcaeaoffline.core.database.externals.arcaea.SonglistParser
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPackageHelper
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream


class DatabaseManageViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    private fun toast(context: Context, string: String, length: Int = Toast.LENGTH_LONG) {
        // https://stackoverflow.com/a/34970752/16484891
        // make code running in main thread, CC BY-SA 4.0
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, string, length).show()
        }
    }

    private suspend fun importPacklist(inputStream: InputStream, context: Context? = null) {
        val result = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        val packs = PacklistParser(result).parsePack().toTypedArray()
        repositoryContainer.packRepository.upsertAll(*packs)

        if (context != null) {
            val packText = String.format(
                context.resources.getQuantityString(
                    R.plurals.database_packlist_imported, packs.size
                ), packs.size
            )

            toast(context, packText)
        }
        Log.i(LOG_TAG, "${packs.size} packs updated")
    }

    suspend fun importPacklist(uri: Uri, context: Context) {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return
        importPacklist(inputStream, context)
    }

    private suspend fun importSonglist(inputStream: InputStream, context: Context? = null) {
        val result = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        val songs = SonglistParser(result).parseSong().toTypedArray()
        val difficulties = SonglistParser(result).parseDifficulty().toTypedArray()
        repositoryContainer.songRepository.upsertAll(*songs)
        repositoryContainer.difficultyRepository.upsertAll(*difficulties)

        if (context != null) {
            val songText = String.format(
                context.resources.getQuantityString(
                    R.plurals.database_songlist_song_imported, songs.size
                ), songs.size
            )
            val difficultyText = String.format(
                context.resources.getQuantityString(
                    R.plurals.database_songlist_difficulty_imported, difficulties.size
                ), difficulties.size
            )

            toast(context, songText + "\n" + difficultyText)
        }
        Log.i(LOG_TAG, "${songs.size} songs updated")
        Log.i(LOG_TAG, "${difficulties.size} difficulties updated")
    }

    suspend fun importSonglist(uri: Uri, context: Context) {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return
        importSonglist(inputStream, context)
    }

    private suspend fun importArcaeaApkFromZipInputStream(
        zipInputStream: ZipInputStream, context: Context? = null
    ) {
        var entry = zipInputStream.nextEntry

        var packlistFound = false
        var songlistFound = false
        while (entry != null) {
            if (entry.name == ArcaeaPackageHelper.APK_PACKLIST_FILE_ENTRY_NAME) {
                packlistFound = true
                val buffer = ByteArrayOutputStream()
                IOUtils.copy(zipInputStream, buffer)
                importPacklist(ByteArrayInputStream(buffer.toByteArray()), context)
            }

            if (entry.name == ArcaeaPackageHelper.APK_SONGLIST_FILE_ENTRY_NAME) {
                songlistFound = true
                val buffer = ByteArrayOutputStream()
                IOUtils.copy(zipInputStream, buffer)
                importSonglist(ByteArrayInputStream(buffer.toByteArray()), context)
            }

            entry = zipInputStream.nextEntry
        }

        if (context != null) {
            if (!packlistFound) toast(context, "packlist not found!")
            if (!songlistFound) toast(context, "songlist not found!")
        }
    }

    suspend fun importArcaeaApkFromInputStream(inputStream: InputStream, context: Context) {
        ZipInputStream(inputStream).use {
            importArcaeaApkFromZipInputStream(it, context)
        }
    }

    suspend fun importArcaeaApkFromInstalled(context: Context) {
        ArcaeaPackageHelper(context).getApkZipFile().use { zipFile ->
            val packlistEntry = zipFile.getEntry(ArcaeaPackageHelper.APK_PACKLIST_FILE_ENTRY_NAME)
            val songlistEntry = zipFile.getEntry(ArcaeaPackageHelper.APK_SONGLIST_FILE_ENTRY_NAME)

            withContext(Dispatchers.IO) {
                val packlistInputStream = zipFile.getInputStream(packlistEntry)
                importPacklist(packlistInputStream, context)

                val songlistInputStream = zipFile.getInputStream(songlistEntry)
                importSonglist(songlistInputStream, context)
            }
        }
    }

    suspend fun importChartsInfoDatabase(fileUri: Uri, context: Context) {
        val inputStream = context.contentResolver.openInputStream(fileUri) ?: return

        val databaseCopied = File(context.cacheDir, "chart_info_database_copy.db")
        if (databaseCopied.exists()) databaseCopied.delete()

        inputStream.use {
            IOUtils.copy(it, databaseCopied.outputStream())
        }

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
                    toast(context, "Database invalid!")
                    return
                }

                do {
                    val songId = it.getString(songIdColumnIndex)
                    val ratingClass = it.getInt(ratingClassColumnIndex)
                    val constant = it.getInt(constantColumnIndex)
                    val notes = it.getIntOrNull(it.getColumnIndex("notes"))

                    val chartInfo = ChartInfo(
                        songId = songId,
                        ratingClass = ArcaeaScoreRatingClass.fromInt(ratingClass),
                        constant = constant,
                        notes = notes
                    )
                    chartInfoList.add(chartInfo)
                } while (it.moveToNext())
            }
        }

        val affectedRows = repositoryContainer.chartInfoRepository.insertAll(
            *chartInfoList.toTypedArray()
        )

        toast(
            context, String.format(
                context.resources.getString(R.string.database_chart_info_imported),
                affectedRows.size,
            )
        )

        databaseCopied.delete()
    }

    suspend fun exportScores(outputStream: OutputStream) {
        val content = ArcaeaOfflineExportScore(repositoryContainer.scoreRepository).toJsonString()
        if (content != null) {
            IOUtils.write(content, outputStream)
        }
    }

    companion object {
        const val LOG_TAG = "DatabaseManageViewModel"
    }
}
