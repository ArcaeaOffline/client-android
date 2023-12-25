package xyz.sevive.arcaeaoffline.permissions.storage

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import org.threeten.bp.Instant
import java.io.File
import java.io.FileOutputStream


private fun checkSaveToGalleryPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return true

    val checkPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    return checkPermission == PackageManager.PERMISSION_GRANTED
}

class SaveBitmapToGallery(private val bitmap: Bitmap) {
    companion object {
        const val DIRECTORY = "Arcaea Offline"
        const val TAG = "SaveBitmapToGallery"

        fun checkPermission(context: Context): Boolean {
            return checkSaveToGalleryPermission(context)
        }
    }

    /**
     * https://stackoverflow.com/a/73554532/16484891
     *
     * CC BY-SA 4.0
     * @see <a href="https://stackoverflow.com/a/73554532/16484891">link</a>
     */
    private fun saveBitmap(
        context: Context,
        filename: String,
        compressFormat: Bitmap.CompressFormat,
        quality: Int,
    ): Boolean {
        Log.i(TAG, "Saving $filename to gallery")

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!checkPermission(context)) return false

            val parentDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                DIRECTORY
            )

            if (!parentDir.exists()) {
                Log.i(TAG, "Creating parent dir, success: ${parentDir.mkdirs()}")
            }
            FileOutputStream(File(parentDir, filename)).use {
                bitmap.compress(compressFormat, quality, it)
            }
        } else {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                // put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/$DIRECTORY"
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let { resolver.openOutputStream(it) }
                ?.use { bitmap.compress(compressFormat, quality, it) }

            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            uri?.also { resolver.update(it, values, null, null) }
        }

        return true
    }

    private fun getDefaultFileBaseName(): String {
        return "arcaea-offline-${Instant.now().toEpochMilli()}"
    }

    fun saveJpg(
        context: Context,
        fileBaseName: String = getDefaultFileBaseName(),
        quality: Int = 100,
    ) = saveBitmap(context, "$fileBaseName.jpg", Bitmap.CompressFormat.JPEG, quality)

    fun savePng(
        context: Context,
        fileBaseName: String = getDefaultFileBaseName(),
        quality: Int = 100,
    ) = saveBitmap(context, "$fileBaseName.png", Bitmap.CompressFormat.PNG, quality)
}

