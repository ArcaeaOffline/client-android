package xyz.sevive.arcaeaoffline.core.helpers

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import xyz.sevive.arcaeaoffline.core.ocr.ImagePhashDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcr
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ArcaeaNotInstalledException : Exception() {
    override val message = "Arcaea not installed!"
}

class ArcaeaHelper(context: Context) {
    private val packageManager = context.packageManager

    val jacketsCacheDir = File(context.cacheDir, "arcaea" + File.separator + "jackets")
    val partnerIconsCacheDir = File(context.cacheDir, "arcaea" + File.separator + "partner_icons")

    // debug only
    // val jacketsCacheDir = File("/storage/emulated/0/Documents/ArcaeaOffline/jackets")
    // val partnerIconsCacheDir = File("/storage/emulated/0/Documents/ArcaeaOffline/partner_icons")

    val tempPhashDatabaseFile = File(context.cacheDir, tempPhashDatabaseFilename)

    fun getPackageInfo(): PackageInfo? {
        return try {
            packageManager.getPackageInfo(ArcaeaPackageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * @throws ArcaeaNotInstalledException if Arcaea isn't installed
     */
    fun getPackageInfoOrFail(): PackageInfo {
        return getPackageInfo() ?: throw ArcaeaNotInstalledException()
    }

    fun isInstalled(): Boolean {
        return getPackageInfo() != null
    }

    fun getIcon(): Drawable? {
        getPackageInfoOrFail()

        return try {
            packageManager.getApplicationIcon(ArcaeaPackageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    suspend fun getApkZipFile(): ZipFile {
        getPackageInfoOrFail()

        val appInfo = packageManager.getApplicationInfo(ArcaeaPackageName, 0)

        return withContext(Dispatchers.IO) {
            ZipFile(appInfo.publicSourceDir)
        }
    }

    private suspend fun extractJackets() {
        val apkZipFile = getApkZipFile()
        val entries = apkZipFile.entries()

        if (!jacketsCacheDir.exists()) {
            jacketsCacheDir.mkdirs()
        }

        // Map<songId, List<ZipEntry>>
        val jacketZipEntries = mutableMapOf<String, MutableList<ZipEntry>>()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()

            // checks if this entry is under the `assets/songs` folder
            val entryInSongFolder = entry.name.startsWith(SongsFolderEntryName)
            if (!entryInSongFolder) continue

            // checks the directory depth
            val entryParts = entry.name.split("/")
            if (entryParts.size != 4) continue

            // prepare entry map
            val songId = entryParts[2].replace("dl_", "")
            if (jacketZipEntries[songId] == null) {
                jacketZipEntries[songId] = mutableListOf()
            }

            // checks the filename
            val filename = entryParts[3]
            if (JacketFilenameRegex.find(filename) != null) {
                jacketZipEntries[songId]!!.add(entry)
            }
        }

        jacketZipEntries.entries.forEach { mapEntry ->
            val songId = mapEntry.key
            val zipEntries = mapEntry.value

            zipEntries.forEach { entry ->
                val entryFilename = FilenameUtils.getBaseName(entry.name).replace("1080_", "")
                val entryExtension = FilenameUtils.getExtension(entry.name)

                val filename = if (entryFilename.contains("base")) {
                    "$songId.$entryExtension"
                } else {
                    "${songId}_$entryFilename.$entryExtension"
                }

                File(jacketsCacheDir, filename).outputStream().use {
                    IOUtils.copy(apkZipFile.getInputStream(entry), it)
                }
            }
        }
    }

    private suspend fun extractPartnerIcons() {
        val apkZipFile = getApkZipFile()
        val entries = apkZipFile.entries()

        if (!partnerIconsCacheDir.exists()) {
            partnerIconsCacheDir.mkdirs()
        }

        // Map<iconId, ZipEntry>
        val partnerIconZipEntries = mutableMapOf<String, ZipEntry>()

        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()

            // checks if this entry is under the `assets/char/` folder
            val entryInCharFolder = entry.name.startsWith(CharFolderEntryName)
            if (!entryInCharFolder) continue

            // checks the directory depth
            val entryParts = entry.name.split("/")
            if (entryParts.size != 3) continue

            // checks the filename
            val filename = FilenameUtils.getBaseName(entryParts[2])
            if (PartnerIconFilenameRegex.find(filename) != null) {
                val partnerIcon = filename.replace("_icon", "")
                partnerIconZipEntries[partnerIcon] = entry
            }
        }

        partnerIconZipEntries.entries.forEach { mapEntry ->
            val partnerId = mapEntry.key
            val zipEntry = mapEntry.value

            val entryExtension = FilenameUtils.getExtension(zipEntry.name)

            val filename = "$partnerId.$entryExtension"

            File(partnerIconsCacheDir, filename).outputStream().use {
                IOUtils.copy(apkZipFile.getInputStream(zipEntry), it)
            }
        }
    }

    private fun buildPhashDatabaseCleanUp() {
        if (jacketsCacheDir.exists()) FileUtils.cleanDirectory(jacketsCacheDir)
        if (partnerIconsCacheDir.exists()) FileUtils.cleanDirectory(partnerIconsCacheDir)
    }

    suspend fun buildPhashDatabase() {
        buildPhashDatabaseCleanUp()
        if (tempPhashDatabaseFile.exists()) tempPhashDatabaseFile.delete()

        try {
            extractJackets()
            extractPartnerIcons()

            val mats = mutableListOf<Mat>()
            val labels = mutableListOf<String>()

            jacketsCacheDir.listFiles()?.forEach {
                mats.add(Imgcodecs.imread(it.path, Imgcodecs.IMREAD_GRAYSCALE))
                labels.add(FilenameUtils.getBaseName(it.name).replace(JacketRenameRegex, ""))
            }

            partnerIconsCacheDir.listFiles()?.forEach {
                val mat = Imgcodecs.imread(it.path, Imgcodecs.IMREAD_GRAYSCALE)
                mats.add(DeviceOcr.preprocessPartnerIcon(mat))
                labels.add("partner_icon||${FilenameUtils.getBaseName(it.name)}")
            }

            ImagePhashDatabase.build(tempPhashDatabaseFile, mats, labels)
        } finally {
            buildPhashDatabaseCleanUp()
        }
    }

    companion object {
        const val ArcaeaPackageName = "moe.low.arc"
        // const val PacklistEntryName = "assets/songs/packlist"
        // const val SonglistEntryName = "assets/songs/songlist"

        const val SongsFolderEntryName = "assets/songs/"
        val JacketFilenameRegex = """^(1080_)?(0|1|2|3|base)\.(jpg|png)$""".toRegex()
        val JacketRenameRegex = """_.*$""".toRegex()

        const val CharFolderEntryName = "assets/char/"
        val PartnerIconFilenameRegex = """^\d+u?_icon$""".toRegex()

        const val tempPhashDatabaseFilename = "phash_temp.db"
    }
}
