package xyz.sevive.arcaeaoffline.helpers

import android.graphics.BitmapFactory
import android.net.Uri
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.io.asInputStream
import kotlinx.io.buffered
import xyz.sevive.arcaeaoffline.core.ocr.device.CropBlackEdges
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.DeviceRoisAutoSelector
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.DeviceRoisAutoSelectorResult
import xyz.sevive.arcaeaoffline.core.ocr.opencv.decodeBgrImage
import xyz.sevive.arcaeaoffline.core.ocr.opencv.use

object OcrQueueHelper {
    private const val MAX_FILE_SIZE_BYTES = 30L * 1024 * 1024

    suspend fun isUriImage(uri: Uri): Boolean =
        withContext(Dispatchers.IO) {
            async {
                // Know if a file is an image in Java/Android
                // https://stackoverflow.com/a/18499840/16484891
                // CC BY-SA 3.0
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                PlatformFile(uri).source().buffered().asInputStream().use { inputStream ->
                    BitmapFactory.decodeStream(inputStream, null, options)
                }

                options.outWidth != -1 && options.outHeight != -1
            }.await()
        }

    suspend fun isUriArcaeaImage(uri: Uri): Boolean =
        withContext(Dispatchers.IO) {
            async {
                val file = PlatformFile(uri)
                if (file.size() > MAX_FILE_SIZE_BYTES) return@async false

                decodeBgrImage(file.readBytes()).use { img ->
                    CropBlackEdges.cropOrClone(img).use { cropped ->
                        DeviceRoisAutoSelector.select(cropped) != DeviceRoisAutoSelectorResult.UNKNOWN
                    }
                }
            }.await()
        }
}
