package xyz.sevive.arcaeaoffline.ui.database

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import xyz.sevive.arcaeaoffline.core.database.externals.arcaea.PacklistParser
import xyz.sevive.arcaeaoffline.core.database.externals.arcaea.SonglistParser
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipFile

class DatabaseManageViewModel(private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer) :
    ViewModel() {

    suspend fun importPacklist(inputStream: InputStream) {
        val result = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        repositoryContainer.packRepository.upsertAll(
            *PacklistParser(result).parsePack().toTypedArray()
        )
    }

    suspend fun importSonglist(inputStream: InputStream) {
        val result = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        repositoryContainer.songRepository.upsertAll(
            *SonglistParser(result).parseSong().toTypedArray()
        )
        repositoryContainer.difficultyRepository.upsertAll(
            *SonglistParser(result).parseDifficulty().toTypedArray()
        )
    }

    suspend fun importArcaeaApkFromInstalled(context: Context) {
        val packageManager = context.packageManager
        val arcaeaAppInfo = packageManager.getApplicationInfo("moe.low.arc", 0)

        val arcaeaApkPath = arcaeaAppInfo.publicSourceDir


        val arcaeaApkZipFile = withContext(Dispatchers.IO) {
            ZipFile(arcaeaApkPath)
        }
        val packlistZipEntry = arcaeaApkZipFile.getEntry("assets/songs/packlist")
        val songlistZipEntry = arcaeaApkZipFile.getEntry("assets/songs/songlist")

        val packlistInputStream = withContext(Dispatchers.IO) {
            arcaeaApkZipFile.getInputStream(packlistZipEntry)
        }
        importPacklist(packlistInputStream)

        val songlistInputStream = withContext(Dispatchers.IO) {
            arcaeaApkZipFile.getInputStream(songlistZipEntry)
        }
        importSonglist(songlistInputStream)
    }
}
