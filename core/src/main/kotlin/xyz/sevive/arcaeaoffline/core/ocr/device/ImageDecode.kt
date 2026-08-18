package xyz.sevive.arcaeaoffline.core.ocr.device

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.InputStream

object ImageDecode {
    /**
     * Decodes an image into a BGR Mat via the Android bitmap pipeline: pixel
     * data stays in Skia's native heap and never passes through a Java
     * byte[] (whole-file arrays in the Java heap may cause severe GC pressure).
     * Returns null if the stream is not a decodable image.
     */
    fun decode(inputStream: InputStream): Mat? {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = BitmapFactory.decodeStream(inputStream, null, options) ?: return null

        val rgba = Mat()
        try {
            Utils.bitmapToMat(bitmap, rgba)
        } finally {
            bitmap.recycle()
        }

        val bgr = Mat()
        try {
            // bitmapToMat yields RGBA; convert to match Imgcodecs.IMREAD_COLOR
            Imgproc.cvtColor(rgba, bgr, Imgproc.COLOR_RGBA2BGR)
            return bgr
        } finally {
            rgba.release()
        }
    }
}
