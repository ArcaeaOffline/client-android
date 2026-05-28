package xyz.sevive.arcaeaoffline.helpers.context

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath

// find activity from context
// https://stackoverflow.com/a/69235067/16484891
// CC BY-SA 4.0
fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

fun Context.getFilename(uri: Uri): String? = runCatching { PlatformFile(uri).name }.getOrNull()

suspend fun Context.copyToCache(
    uri: Uri,
    filename: String,
): Path? {
    val cachePath = (this.externalCacheDir ?: this.cacheDir).toOkioPath().resolve(filename)
    return try {
        val bytes = PlatformFile(uri).readBytes()
        FileSystem.SYSTEM.write(cachePath) { write(bytes) }
        cachePath
    } catch (e: Exception) {
        null
    }
}

fun Context.getFileSize(uri: Uri): Long? = runCatching { PlatformFile(uri).size() }.getOrNull()
