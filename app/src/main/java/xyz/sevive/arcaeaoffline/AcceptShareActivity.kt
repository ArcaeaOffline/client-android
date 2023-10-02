package xyz.sevive.arcaeaoffline

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.apache.commons.io.IOUtils
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.ocr.ocrDigitsByContourKnn
import xyz.sevive.arcaeaoffline.ocr.rois.definition.DeviceAutoRoisT2
import xyz.sevive.arcaeaoffline.ocr.rois.extractor.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.ocr.rois.masker.DeviceAutoRoisMaskerT2
import xyz.sevive.arcaeaoffline.settings.SettingsOcr

class AcceptShareActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.createNotificationChannel()

        // https://www.cnblogs.com/daner1257/p/5581443.html
        val action = intent.action
        val type = intent.type
        if (action.equals(Intent.ACTION_SEND) && type != null && type.startsWith("image/")) {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            //接收多张图片: ArrayList<Uri> uris=intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                try {
                    val inputStream =
                        contentResolver.openInputStream(uri) // get an input stream from the Uri

                    val bytes: ByteArray =
                        IOUtils.toByteArray(inputStream) // convert the input stream to a byte array

                    val image = Imgcodecs.imdecode(
                        MatOfByte(*bytes), Imgcodecs.IMREAD_UNCHANGED
                    ) // decode the byte array to a Mat object using OpenCV

                    val rois = DeviceAutoRoisT2(
                        image.size().width.toInt(), image.size().height.toInt()
                    )

                    val e = DeviceRoisExtractor(rois, image)
                    val m = DeviceAutoRoisMaskerT2()

                    val pureRoi = m.pure(e.pure)
                    val farRoi = m.far(e.far)
                    val lostRoi = m.lost(e.lost)

                    val knnModel = KNearest.load(
                        SettingsOcr(this.baseContext).knnModelFile().path
                    )

                    val pure = ocrDigitsByContourKnn(pureRoi, knnModel)
                    val far = ocrDigitsByContourKnn(farRoi, knnModel)
                    val lost = ocrDigitsByContourKnn(lostRoi, knnModel)

                    Log.d("result", "do got result p$pure f$far l$lost")

                    var builder = NotificationCompat.Builder(this, "SHARE_OCR_RESULT")
                        .setSmallIcon(R.drawable.ic_notification_info)
                        .setContentTitle("OCR Result")
                        .setContentText("PURE $pure, FAR $far, LOST $lost")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                    with(NotificationManagerCompat.from(this)) {
                        // notificationId is a unique int for each notification that you must define.
                        notify(283375, builder.build())
                    }

                    // Toast.makeText(this.applicationContext, "PURE: $res", Toast.LENGTH_SHORT)

                } catch (e: Exception) {
                    var builder = NotificationCompat.Builder(this, "SHARE_OCR_RESULT")
                        .setSmallIcon(R.drawable.ic_notification_info)
                        .setContentTitle("OCR Error")
                        .setContentText(e.message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                    with(NotificationManagerCompat.from(this)) {
                        // notificationId is a unique int for each notification that you must define.
                        notify(283375, builder.build())
                    }

                }
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Share OCR Result"  // getString(R.string.channel_name)
            val descriptionText =
                "This is description"  // getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("SHARE_OCR_RESULT", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
