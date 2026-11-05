package xyz.sevive.arcaeaoffline.permissions.storage

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import co.touchlab.kermit.Logger
import java.io.File
import java.io.IOException

/** Writes image bytes into the public Pictures gallery. */
object SaveImageToGallery {
    const val DIRECTORY = "Arcaea Offline"
    const val LOG_TAG = "SaveImageToGallery"

    private val logger = Logger.withTag(LOG_TAG)

    fun checkPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return true

        val checkPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        return checkPermission == PackageManager.PERMISSION_GRANTED
    }

    /**
     * https://stackoverflow.com/a/73554532/16484891
     *
     * CC BY-SA 4.0
     * @see <a href="https://stackoverflow.com/a/73554532/16484891">link</a>
     * @param dateTakenMillis The image's logical date in epoch millis, for gallery apps use it for
     *   timeline ordering on API 29+.
     * @param mimeType Override MediaStore's mime type prediction
     * @throws IOException If writing the image fails, including MediaStore rejecting the entry.
     * @throws SecurityException On API < 29 when [checkPermission] is false.
     */
    fun saveImageBytes(
        context: Context,
        fileName: String,
        bytes: ByteArray,
        dateTakenMillis: Long,
        mimeType: String,
    ) {
        logger.i { "Saving $fileName to gallery" }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            saveImageBytesPreQ(context, fileName, bytes)
        } else {
            saveImageBytesQPlus(context, fileName, bytes, dateTakenMillis, mimeType)
        }
    }

    private fun saveImageBytesPreQ(
        context: Context,
        fileName: String,
        bytes: ByteArray,
    ) {
        if (!checkPermission(context)) {
            throw SecurityException("WRITE_EXTERNAL_STORAGE permission is not granted")
        }

        val parentDir =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                DIRECTORY,
            )

        if (!parentDir.exists()) {
            logger.i { "Creating parent dir ${parentDir.path}, success: ${parentDir.mkdirs()}" }
        }

        // Q+ MediaProvider renames duplicate display names itself.
        // Older API would silently overwrite, so pick a free name instead.
        var target = File(parentDir, fileName)
        if (target.exists()) {
            val dot = fileName.lastIndexOf('.')
            val base = if (dot < 0) fileName else fileName.substring(0, dot)
            val ext = if (dot < 0) "" else fileName.substring(dot)

            var index = 1
            while (target.exists()) {
                target = File(parentDir, "$base ($index)$ext")
                index++
            }
        }
        try {
            target.outputStream().use { it.write(bytes) }
        } catch (e: Exception) {
            // A failed save must not leave a partial file behind
            target.delete()
            throw e
        }
    }

    private fun saveImageBytesQPlus(
        context: Context,
        fileName: String,
        bytes: ByteArray,
        dateTakenMillis: Long,
        mimeType: String,
    ) {
        val values =
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/$DIRECTORY",
                )
                put(MediaStore.Images.Media.DATE_TAKEN, dateTakenMillis)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

        val resolver = context.contentResolver
        val uri =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IOException("Failed to create a MediaStore entry for $fileName")

        try {
            val outputStream =
                resolver.openOutputStream(uri)
                    ?: throw IOException("Failed to open the MediaStore entry for writing")

            outputStream.use { it.write(bytes) }

            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            if (resolver.update(uri, values, null, null) == 0) {
                throw IOException("Failed to finalize the MediaStore entry for $fileName")
            }
        } catch (e: Exception) {
            // A failed save must not leave the IS_PENDING placeholder behind
            runCatching { resolver.delete(uri, null, null) }
                .onFailure { logger.w(it) { "Failed to remove the pending MediaStore entry" } }
            throw e
        }
    }
}
