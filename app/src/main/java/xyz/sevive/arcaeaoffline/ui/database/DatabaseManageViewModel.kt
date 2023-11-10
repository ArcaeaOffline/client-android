package xyz.sevive.arcaeaoffline.ui.database

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.externals.arcaea.PacklistParser
import xyz.sevive.arcaeaoffline.core.database.externals.arcaea.SonglistParser
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream


class DatabaseManageViewModel(private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer) :
    ViewModel() {
    fun isArcaeaInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.low.arc", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getArcaeaIconFromInstalled(context: Context): Drawable? {
        return try {
            context.packageManager.getApplicationIcon("moe.low.arc")
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

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

    companion object {
        const val PacklistEntryName = "assets/songs/packlist"
        const val SonglistEntryName = "assets/songs/songlist"
    }
}
