package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
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

class ArcaeaAssetAbsenceException : Exception() {
    override val message = "Cannot find the requested asset in Arcaea!"
}

class ArcaeaPackageHelper(context: Context) {
    private val packageManager = context.packageManager

    private val arcaeaExtractRootCacheDir = File(context.cacheDir, "arcaea")
    private val jacketsCacheDir = File(arcaeaExtractRootCacheDir, "jackets")
    private val partnerIconsCacheDir = File(arcaeaExtractRootCacheDir, "partner_icons")

    // debug only
    // val jacketsCacheDir = File("/storage/emulated/0/Documents/ArcaeaOffline/jackets")
    // val partnerIconsCacheDir = File("/storage/emulated/0/Documents/ArcaeaOffline/partner_icons")

    val tempPhashDatabaseFile = File(context.cacheDir, TEMP_PHASH_DATABASE_FILENAME)

    private fun getPackageInfo(): PackageInfo? {
        return try {
            packageManager.getPackageInfo(ARCAEA_PACKAGE_NAME, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * @throws ArcaeaNotInstalledException if Arcaea isn't installed
     */
    private fun getPackageInfoOrFail(): PackageInfo {
        return getPackageInfo() ?: throw ArcaeaNotInstalledException()
    }

    fun isInstalled(): Boolean {
        return getPackageInfo() != null
    }

    fun getIcon(): Drawable? {
        if (!isInstalled()) return null
        return packageManager.getApplicationIcon(ARCAEA_PACKAGE_NAME)
    }

    fun getApkZipFile(): ZipFile {
        getPackageInfoOrFail()

        val appInfo = packageManager.getApplicationInfo(ARCAEA_PACKAGE_NAME, 0)

        return ZipFile(appInfo.publicSourceDir)
    }

    fun getPacklistEntry(): ZipEntry? {
        if (!isInstalled()) return null
        return getApkZipFile().use {
            it.getEntry(APK_PACKLIST_FILE_ENTRY_NAME)
        }
    }

    fun getSonglistEntry(): ZipEntry? {
        if (!isInstalled()) return null
        return getApkZipFile().use {
            it.getEntry(APK_SONGLIST_FILE_ENTRY_NAME)
        }
    }

    /**
     * If the [filename] matches a jacket file's naming pattern, return the
     * extract output filename of it.
     * Otherwise, return null.
     *
     * Example [filename]s are:
     * - assets/songs/dl_climax/1080_base.jpg
     * - assets/songs/dl_antithese/1080_3.jpg
     * - files/cb/active/songs/dl_climax/1080_base.jpg
     * - files/cb/active/songs/dl_antithese/1080_3.jpg
     */
    private fun jacketExtractFilename(filename: String): String? {
        val filenameParts = filename.split("/")

        if (filenameParts.size < 2) return null

        val tailFilename = FilenameUtils.getName(filenameParts[filenameParts.size - 1])
        if (JACKET_FILENAME_REGEX.find(tailFilename) == null) return null

        val songId = filenameParts[filenameParts.size - 2].replace("dl_", "")

        val baseFilename = FilenameUtils.getBaseName(filename)
        val difficulty = baseFilename.replace("1080_", "")
        val ext = FilenameUtils.getExtension(filename)

        val finalFilename = if (difficulty == "base") {
            "${songId}.${ext}"
        } else {
            "${songId}_${difficulty}.${ext}"
        }
        Log.d(LOG_TAG, "jacketExtractFilename: mapping [$filename] to [$finalFilename]")
        return finalFilename
    }

    /**
     * If the [filename] matches a partner icon file's naming pattern, return the
     * extract output filename of it.
     * Otherwise, return null.
     *
     * Example [filename]s are:
     * - assets/char/75_icon.png
     * - files/cb/active/char/75_icon.png
     */
    private fun partnerIconExtractFilename(filename: String): String? {
        val filenameParts = filename.split("/")

        if (filenameParts.size < 2) return null
        if (filenameParts[filenameParts.size - 2] != "char") return null

        val tailFilename = FilenameUtils.getName(filenameParts[filenameParts.size - 1])
        if (PARTNER_ICON_FILENAME_REGEX.find(tailFilename) == null) return null

        val baseFilename = FilenameUtils.getBaseName(filenameParts[filenameParts.size - 1])
        val partnerId = baseFilename.replace("_icon", "")
        val ext = FilenameUtils.getExtension(filename)

        val finalFilename = "${partnerId}.${ext}"
        Log.d(LOG_TAG, "partnerIconExtractFilename: mapping [$filename] to [$finalFilename]")
        return finalFilename
    }

    private suspend fun extractAssetBase(outputMapping: Map<ZipEntry, File>) {
        if (outputMapping.isEmpty()) {
            Log.w(LOG_TAG, "extractAssetBase: outputMapping empty, returning")
            return
        }

        withContext(Dispatchers.IO) {
            Log.d(LOG_TAG, "extractAssetBase: extracting ${outputMapping.size} entries")
            getApkZipFile().use { zipFile ->
                outputMapping.entries.forEach { mapEntry ->
                    val zipEntry = mapEntry.key
                    val outputFile = mapEntry.value

                    outputFile.outputStream().use {
                        IOUtils.copy(zipFile.getInputStream(zipEntry), it)
                    }
                }
            }
        }
    }

    private fun filterApkEntries(filterBlock: (ZipEntry) -> Boolean): List<ZipEntry> {
        val apkZipFile = getApkZipFile()
        val entries = mutableListOf<ZipEntry>()
        apkZipFile.use {
            val zipEntries = apkZipFile.entries()
            while (zipEntries.hasMoreElements()) {
                val zipEntry = zipEntries.nextElement()

                if (!filterBlock(zipEntry)) continue
                entries.add(zipEntry)
            }
        }
        return entries.toList()
    }

    fun apkJacketZipEntries(): List<ZipEntry> {
        return filterApkEntries {
            // check file parent
            val entryInSongFolder = it.name.startsWith(APK_SONGS_FOLDER_ENTRY_NAME)
            if (!entryInSongFolder) return@filterApkEntries false

            // checks the directory depth
            val entryParts = it.name.split("/")
            if (entryParts.size != 4) return@filterApkEntries false

            jacketExtractFilename(it.name) != null
        }
    }

    private suspend fun extractJackets() {
        if (!jacketsCacheDir.exists()) {
            jacketsCacheDir.mkdirs()
        }

        val entries = apkJacketZipEntries()
        val outputMapping = entries.mapNotNull { zipEntry ->
            jacketExtractFilename(zipEntry.name)?.let { filename ->
                Pair(zipEntry, File(jacketsCacheDir, filename))
            }
        }.toMap()
        extractAssetBase(outputMapping)
    }

    fun apkPartnerIconZipEntries(): List<ZipEntry> {
        return filterApkEntries {
            // check file parent
            val entryInCharFolder = it.name.startsWith(APK_CHAR_FOLDER_ENTRY_NAME)
            if (!entryInCharFolder) return@filterApkEntries false

            // checks the directory depth
            val entryParts = it.name.split("/")
            if (entryParts.size != 3) return@filterApkEntries false

            partnerIconExtractFilename(it.name) != null
        }
    }

    private suspend fun extractPartnerIcons() {
        if (!partnerIconsCacheDir.exists()) {
            partnerIconsCacheDir.mkdirs()
        }

        val entries = apkPartnerIconZipEntries()
        val outputMapping = entries.mapNotNull { zipEntry ->
            partnerIconExtractFilename(zipEntry.name)?.let { filename ->
                Pair(zipEntry, File(partnerIconsCacheDir, filename))
            }
        }.toMap()
        extractAssetBase(outputMapping)
    }

    private fun buildPhashDatabaseCleanUp() {
        if (jacketsCacheDir.exists()) FileUtils.cleanDirectory(jacketsCacheDir)
        if (partnerIconsCacheDir.exists()) FileUtils.cleanDirectory(partnerIconsCacheDir)
    }

    suspend fun buildPhashDatabase(
        progressCallback: (progress: Int, total: Int) -> Unit = { _, _ -> }
    ) {
        buildPhashDatabaseCleanUp()
        if (tempPhashDatabaseFile.exists()) tempPhashDatabaseFile.delete()

        try {
            extractJackets()
            extractPartnerIcons()

            val mats = mutableListOf<Mat>()
            val labels = mutableListOf<String>()

            jacketsCacheDir.listFiles()?.forEach {
                mats.add(Imgcodecs.imread(it.path, Imgcodecs.IMREAD_GRAYSCALE))
                labels.add(FilenameUtils.getBaseName(it.name).replace(JACKET_RENAME_REGEX, ""))
            }

            partnerIconsCacheDir.listFiles()?.forEach {
                val mat = Imgcodecs.imread(it.path, Imgcodecs.IMREAD_GRAYSCALE)
                mats.add(DeviceOcr.preprocessPartnerIcon(mat))
                labels.add("partner_icon||${FilenameUtils.getBaseName(it.name)}")
            }

            ImagePhashDatabase.build(
                tempPhashDatabaseFile,
                mats,
                labels,
                progressCallback = progressCallback,
            )
        } finally {
            buildPhashDatabaseCleanUp()
        }
    }

    companion object {
        const val LOG_TAG = "ArcaeaPackageHelper"

        const val ARCAEA_PACKAGE_NAME = "moe.low.arc"
        const val APK_PACKLIST_FILE_ENTRY_NAME = "assets/songs/packlist"
        const val APK_SONGLIST_FILE_ENTRY_NAME = "assets/songs/songlist"
        const val APK_SONGS_FOLDER_ENTRY_NAME = "assets/songs/"
        const val APK_CHAR_FOLDER_ENTRY_NAME = "assets/char/"

        val JACKET_FILENAME_REGEX = """^(1080_)?(0|1|2|3|4|base)\.(jpg|png)$""".toRegex()
        val JACKET_RENAME_REGEX = """_.*$""".toRegex()

        val PARTNER_ICON_FILENAME_REGEX = """^\d+u?_icon\.(jpg|png)$""".toRegex()

        const val TEMP_PHASH_DATABASE_FILENAME = "phash_temp.db"
    }
}
