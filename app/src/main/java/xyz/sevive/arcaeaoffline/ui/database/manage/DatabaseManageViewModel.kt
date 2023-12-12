package xyz.sevive.arcaeaoffline.ui.database.manage

import android.annotation.SuppressLint
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
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.export.ArcaeaOfflineExportScore
import xyz.sevive.arcaeaoffline.core.database.externals.arcaea.PacklistParser
import xyz.sevive.arcaeaoffline.core.database.externals.arcaea.SonglistParser
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipFile
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

    suspend fun importPacklist(inputStream: InputStream, context: Context? = null) {
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
        Log.i("Database", "${packs.size} pack(s) updated")
    }

    suspend fun importSonglist(inputStream: InputStream, context: Context? = null) {
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
        Log.i("Database", "${songs.size} song(s) updated")
        Log.i("Database", "${difficulties.size} difficulty(ies) updated")
    }

    suspend fun importArcaeaApkFromSelect(
        zipInputStream: ZipInputStream, context: Context? = null
    ) {
        var entry = zipInputStream.nextEntry

        while (entry != null) {
            if (entry.name == PacklistEntryName) {
                val buffer = ByteArrayOutputStream()
                IOUtils.copy(zipInputStream, buffer)
                importPacklist(ByteArrayInputStream(buffer.toByteArray()), context)
            }

            if (entry.name == SonglistEntryName) {
                val buffer = ByteArrayOutputStream()
                IOUtils.copy(zipInputStream, buffer)
                importSonglist(ByteArrayInputStream(buffer.toByteArray()), context)
            }

            entry = zipInputStream.nextEntry
        }
    }

    suspend fun importArcaeaApkFromInstalled(context: Context) {
        val packageManager = context.packageManager
        val arcaeaAppInfo = packageManager.getApplicationInfo("moe.low.arc", 0)

        val arcaeaApkPath = arcaeaAppInfo.publicSourceDir

        val arcaeaApkZipFile = withContext(Dispatchers.IO) {
            ZipFile(arcaeaApkPath)
        }
        val packlistZipEntry = arcaeaApkZipFile.getEntry(PacklistEntryName)
        val songlistZipEntry = arcaeaApkZipFile.getEntry(SonglistEntryName)

        val packlistInputStream = withContext(Dispatchers.IO) {
            arcaeaApkZipFile.getInputStream(packlistZipEntry)
        }
        importPacklist(packlistInputStream, context)

        val songlistInputStream = withContext(Dispatchers.IO) {
            arcaeaApkZipFile.getInputStream(songlistZipEntry)
        }
        importSonglist(songlistInputStream, context)
    }

    @SuppressLint("Range")
    suspend fun importChartsInfoDatabase(fileUri: Uri, context: Context) {
        val inputStream = context.contentResolver.openInputStream(fileUri) ?: return

        val databaseCopied = File(context.cacheDir, "chart_info_database_copy.db")
        if (databaseCopied.exists()) databaseCopied.delete()

        inputStream.use {
            IOUtils.copy(it, databaseCopied.outputStream())
        }

        val db = SQLiteDatabase.openDatabase(
            databaseCopied.path, null, SQLiteDatabase.OPEN_READONLY
        )

        val cursor = db.query(
            "charts_info",
            arrayOf("song_id", "rating_class", "constant", "notes"),
            null,
            null,
            null,
            null,
            null,
        )

        val chartInfoList = mutableListOf<ChartInfo>()
        cursor.moveToFirst()
        cursor.use {
            while (it.moveToNext()) {
                val songId = it.getString(it.getColumnIndex("song_id"))
                val ratingClass = it.getInt(it.getColumnIndex("rating_class"))
                val constant = it.getInt(it.getColumnIndex("constant"))
                val notes = it.getIntOrNull(it.getColumnIndex("notes"))

                val chartInfo = ChartInfo(
                    songId = songId, ratingClass = ratingClass, constant = constant, notes = notes
                )
                chartInfoList.add(chartInfo)
            }
        }
        repositoryContainer.chartInfoRepository.upsertAll(*chartInfoList.toTypedArray())
    }

    suspend fun exportScores(outputStream: OutputStream) {
        val content = ArcaeaOfflineExportScore(repositoryContainer.scoreRepository).toJsonString()
        if (content != null) {
            IOUtils.write(content, outputStream)
        }
    }

    companion object {
        const val PacklistEntryName = "assets/songs/packlist"
        const val SonglistEntryName = "assets/songs/songlist"
    }
}
