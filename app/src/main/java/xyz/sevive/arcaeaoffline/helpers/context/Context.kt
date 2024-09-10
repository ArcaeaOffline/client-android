package xyz.sevive.arcaeaoffline.helpers.context

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.provider.OpenableColumns
import org.apache.commons.io.IOUtils
import java.io.File

// find activity from context
// https://stackoverflow.com/a/69235067/16484891
// CC BY-SA 4.0
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// extract the filename from Uri
// https://stackoverflow.com/a/56234417/16484891
// CC BY-SA 4.0
private fun Context.getContentFilename(uri: Uri): String? = runCatching {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
    }
}.getOrNull()

fun Context.getFilename(uri: Uri): String? = when (uri.scheme) {
    ContentResolver.SCHEME_CONTENT -> getContentFilename(uri)
    else -> uri.path?.let(::File)?.name
}

fun Context.copyToCache(uri: Uri, filename: String): File? = runCatching {
    val cacheFile = (this.externalCacheDir ?: this.cacheDir).resolve(filename)
    val inputStream = this.contentResolver.openInputStream(uri) ?: return null
    inputStream.use { IOUtils.copy(it, cacheFile.outputStream()) }
    return cacheFile
}.getOrNull()

fun Context.getFileSize(uri: Uri): Long? = runCatching {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor.getColumnIndexOrThrow(OpenableColumns.SIZE).let(cursor::getLong)
    }
}.getOrNull()
