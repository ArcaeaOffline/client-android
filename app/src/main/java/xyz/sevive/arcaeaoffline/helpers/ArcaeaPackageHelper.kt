package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.buffer
import okio.source
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashItemType
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabaseBuilder
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcr
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ArcaeaPackageHelper(
    context: Context,
) {
    private val logger = Logger.withTag(LOG_TAG)
    private val packageManager = context.packageManager

    private val arcaeaExtractRootCacheDir = context.cacheDir.toOkioPath() / "arcaea"
    private val jacketsCacheDir = arcaeaExtractRootCacheDir / "jackets"
    private val partnerIconsCacheDir = arcaeaExtractRootCacheDir / "partner_icons"

    // debug only
    // val jacketsCacheDir = "/storage/emulated/0/Documents/ArcaeaOffline/jackets".toPath()
    // val partnerIconsCacheDir = "/storage/emulated/0/Documents/ArcaeaOffline/partner_icons".toPath()

    fun getPackageInfo(): PackageInfo? =
        try {
            packageManager.getPackageInfo(ARCAEA_PACKAGE_NAME, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

    fun getIcon(): Drawable? =
        try {
            packageManager.getApplicationIcon(ARCAEA_PACKAGE_NAME)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

    fun getApkZipFile(): ZipFile? {
        try {
            val appInfo = packageManager.getApplicationInfo(ARCAEA_PACKAGE_NAME, 0)
            return ZipFile(appInfo.publicSourceDir)
        } catch (_: PackageManager.NameNotFoundException) {
            return null
        }
    }

    fun getPacklistEntry(): ZipEntry? =
        getApkZipFile()?.use {
            it.getEntry(APK_PACKLIST_FILE_ENTRY_NAME)
        }

    fun getSonglistEntry(): ZipEntry? =
        getApkZipFile()?.use {
            it.getEntry(APK_SONGLIST_FILE_ENTRY_NAME)
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

        val tailFilename = filenameParts.last().toPath().name
        if (JACKET_FILENAME_REGEX.find(tailFilename) == null) return null

        val songId = filenameParts[filenameParts.size - 2].replace("dl_", "")

        val path = filename.toPath()
        val baseFilename = path.name.substringBeforeLast(".")
        val difficulty = baseFilename.replace("1080_", "")
        val ext = path.name.substringAfterLast(".")

        val finalFilename =
            if (difficulty == "base") {
                "$songId.$ext"
            } else {
                "${songId}_$difficulty.$ext"
            }
        logger.v { "jacketExtractFilename: mapping [$filename] to [$finalFilename]" }
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

        val tailFilename = filenameParts.last().toPath().name
        if (PARTNER_ICON_FILENAME_REGEX.find(tailFilename) == null) return null

        val baseFilename =
            filenameParts
                .last()
                .toPath()
                .name
                .substringBeforeLast(".")
        val partnerId = baseFilename.replace("_icon", "")
        val ext = filename.toPath().name.substringAfterLast(".")

        val finalFilename = "$partnerId.$ext"
        logger.v { "partnerIconExtractFilename: mapping [$filename] to [$finalFilename]" }
        return finalFilename
    }

    private suspend fun extractAssetBase(outputMapping: Map<ZipEntry, Path>) {
        if (outputMapping.isEmpty()) {
            logger.w { "extractAssetBase: outputMapping empty, returning" }
            return
        }

        withContext(Dispatchers.IO) {
            logger.d { "extractAssetBase: extracting ${outputMapping.size} entries" }
            getApkZipFile()?.use { zipFile ->
                outputMapping.entries.forEach { mapEntry ->
                    val zipEntry = mapEntry.key
                    val outputFile = mapEntry.value

                    zipFile.getInputStream(zipEntry).use { input ->
                        FileSystem.SYSTEM.sink(outputFile).buffer().use { sink ->
                            sink.writeAll(input.source().buffer())
                        }
                    }
                }
            }
        }
    }

    private fun filterApkEntries(filterBlock: (ZipEntry) -> Boolean): List<ZipEntry> {
        val apkZipFile = getApkZipFile() ?: return emptyList()
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
        FileSystem.SYSTEM.createDirectories(jacketsCacheDir)

        val entries = apkJacketZipEntries()
        val outputMapping =
            entries
                .mapNotNull { zipEntry ->
                    jacketExtractFilename(zipEntry.name)?.let { filename ->
                        Pair(zipEntry, jacketsCacheDir / filename)
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
        FileSystem.SYSTEM.createDirectories(partnerIconsCacheDir)

        val entries = apkPartnerIconZipEntries()
        val outputMapping =
            entries
                .mapNotNull { zipEntry ->
                    partnerIconExtractFilename(zipEntry.name)?.let { filename ->
                        Pair(zipEntry, partnerIconsCacheDir / filename)
                    }
                }.toMap()
        extractAssetBase(outputMapping)
    }

    fun buildHashesDatabaseCleanUp() {
        if (FileSystem.SYSTEM.exists(jacketsCacheDir)) {
            FileSystem.SYSTEM.list(jacketsCacheDir).forEach { FileSystem.SYSTEM.delete(it) }
        }
        if (FileSystem.SYSTEM.exists(partnerIconsCacheDir)) {
            FileSystem.SYSTEM.list(partnerIconsCacheDir).forEach { FileSystem.SYSTEM.delete(it) }
        }
    }

    private fun jacketFileToGrayscaleImage(path: Path): Mat {
        val img = Imgcodecs.imread(path.toString(), Imgcodecs.IMREAD_COLOR)
        val imgGrayscale = Mat()
        Imgproc.cvtColor(img, imgGrayscale, Imgproc.COLOR_BGR2GRAY)
        return imgGrayscale
    }

    private fun partnerIconFileToGrayscaleImage(path: Path): Mat {
        val img = Imgcodecs.imread(path.toString(), Imgcodecs.IMREAD_COLOR)
        val imgGrayscale = Mat()
        Imgproc.cvtColor(img, imgGrayscale, Imgproc.COLOR_BGR2GRAY)
        return DeviceOcr.preprocessPartnerIcon(imgGrayscale)
    }

    suspend fun fillHashesDatabaseBuilderTasks(builder: ImageHashesDatabaseBuilder) {
        buildHashesDatabaseCleanUp()

        extractJackets()
        FileSystem.SYSTEM.list(jacketsCacheDir).forEach {
            builder.addTask(
                ImageHashItemType.JACKET,
                it.name.substringBeforeLast(".").replace(JACKET_RENAME_REGEX, ""),
                input = it,
                inputToGrayscaleImage = ::jacketFileToGrayscaleImage,
            )
        }

        extractPartnerIcons()
        FileSystem.SYSTEM.list(partnerIconsCacheDir).forEach {
            builder.addTask(
                ImageHashItemType.PARTNER_ICON,
                it.name.substringBeforeLast("."),
                input = it,
                inputToGrayscaleImage = ::partnerIconFileToGrayscaleImage,
            )
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
    }
}
