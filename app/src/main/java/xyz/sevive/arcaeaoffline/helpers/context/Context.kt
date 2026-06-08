package xyz.sevive.arcaeaoffline.helpers.context

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.sink
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.source
import io.github.vinceglb.filekit.utils.div
import kotlinx.io.buffered
import kotlinx.io.files.Path

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

fun Context.copyToCache(
    uri: Uri,
    filename: String,
): Path? {
    val cachePath = Path((externalCacheDir ?: cacheDir).absolutePath) / filename
    return try {
        PlatformFile(uri).source().buffered().use { source ->
            PlatformFile(cachePath).sink().buffered().use { sink ->
                source.transferTo(sink)
            }
        }
        cachePath
    } catch (e: Exception) {
        Logger.w(e, tag = "Application") { "Error copying $uri to $cachePath" }
        null
    }
}

fun Context.getFileSize(uri: Uri): Long? = runCatching { PlatformFile(uri).size() }.getOrNull()

fun Context.persistUriPermissions(
    uri: Uri,
    modeFlags: Int,
): Boolean {
    try {
        this.contentResolver.takePersistableUriPermission(uri, modeFlags)
        return true
    } catch (e: Exception) {
        Logger.w(e, tag = "Application") { "Error persisting permission $modeFlags for Uri $uri" }
        return false
    }
}

fun Context.persistUriPermissions(
    uris: List<Uri>,
    modeFlags: Int,
): Map<Uri, Boolean> = uris.associateWith { this.persistUriPermissions(it, modeFlags) }
