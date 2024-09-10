package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import xyz.sevive.arcaeaoffline.core.ocr.device.ScreenshotDetect


object OcrQueueHelper {
    suspend fun isUriImage(uri: Uri, context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            async {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@async false
                // Know if a file is a image in Java/Android
                // https://stackoverflow.com/a/18499840/16484891
                // CC BY-SA 3.0
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                inputStream.use { BitmapFactory.decodeStream(it, null, options) }

                options.outWidth != -1 && options.outHeight != -1
            }.await()
        }
    }

    suspend fun isUriArcaeaImage(uri: Uri, context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            async {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@async false
                val byteArray = inputStream.use { IOUtils.toByteArray(inputStream) }

                val img = Imgcodecs.imdecode(MatOfByte(*byteArray), Imgcodecs.IMREAD_COLOR)
                val imgHsv = Mat()
                Imgproc.cvtColor(img, imgHsv, Imgproc.COLOR_BGR2HSV)

                ScreenshotDetect.isArcaeaScreenshot(imgHsv)
            }.await()
        }
    }
}
