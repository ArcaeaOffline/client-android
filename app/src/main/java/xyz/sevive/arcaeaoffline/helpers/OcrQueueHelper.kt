package xyz.sevive.arcaeaoffline.helpers

import android.graphics.BitmapFactory
import android.net.Uri
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.io.asInputStream
import kotlinx.io.buffered
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import xyz.sevive.arcaeaoffline.core.ocr.device.ScreenshotDetect

object OcrQueueHelper {
    suspend fun isUriImage(
        uri: Uri,
    ): Boolean {
        return withContext(Dispatchers.IO) {
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
    }

    suspend fun isUriArcaeaImage(
        uri: Uri,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            async {
                val byteArray = PlatformFile(uri).readBytes()

                val img = Imgcodecs.imdecode(MatOfByte(*byteArray), Imgcodecs.IMREAD_COLOR)
                val imgHsv = Mat()
                Imgproc.cvtColor(img, imgHsv, Imgproc.COLOR_BGR2HSV)

                ScreenshotDetect.isArcaeaScreenshot(imgHsv)
            }.await()
        }
    }
}
