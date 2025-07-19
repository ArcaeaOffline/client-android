package xyz.sevive.arcaeaoffline.core.ocr.device

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.commons.io.IOUtils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.jvm.optionals.getOrElse
import kotlin.properties.Delegates

object DeviceOcrOnnxHelper {
    private const val LOG_TAG = "OnnxHelper"

    private var imageSize by Delegates.notNull<Size>()
    private var imageShape by Delegates.notNull<LongArray>()

    private lateinit var labels: List<String>
    private lateinit var blankToken: String
    private lateinit var padToken: String

    private val jsonSerializer = Json { ignoreUnknownKeys = true }

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    private data class ModelInfo(
        @SerialName("image_height") val imageHeight: Long,
        @SerialName("image_width") val imageWidth: Long,
        @SerialName("labels") val labels: List<String>,
        @SerialName("blank_token") val blankToken: String,
        @SerialName("pad_token") val padToken: String,
    )

    private fun getOrtEnvironment(): OrtEnvironment {
        return OrtEnvironment.getEnvironment("ocr")
    }

    fun loadModelInfo(context: Context) {
        val modelInfo = jsonSerializer.decodeFromString<ModelInfo>(
            IOUtils.toString(context.assets.open("ocr/model_info.json"))
        )

        Log.d(LOG_TAG, "Loaded model info $modelInfo")

        imageSize = Size(modelInfo.imageWidth.toDouble(), modelInfo.imageHeight.toDouble())
        imageShape = longArrayOf(modelInfo.imageHeight, modelInfo.imageWidth, 3L)
        labels = modelInfo.labels.toList()  // make a copy
        blankToken = modelInfo.blankToken
        padToken = modelInfo.padToken
    }

    private fun readOnnxModelBytes(context: Context): ByteArray {
        return IOUtils.toByteArray(context.assets.open("ocr/model_patched.onnx"))
    }

    /**
     * @see <a href="https://onnx.ai/onnx/repo-docs/Versioning.html#serializing-semver-version-numbers-in-protobuf">ONNX documentation</a>
     *
     * @return arrayOf(major, minor, patch)
     */
    fun modelVersion(version: Long): List<Int> {
        val buffer = ByteBuffer.allocate(Long.SIZE_BYTES).order(ByteOrder.BIG_ENDIAN)
        buffer.putLong(version)
        buffer.flip()

        val major = buffer.short.toInt() shl 16
        val minor = buffer.short.toInt() shl 8
        val patch = buffer.int

        return listOf(major shr 16, minor shr 8, patch)
    }

    fun modelVersionString(version: Long): String {
        return "v" + modelVersion(version).joinToString(".")
    }

    fun createOrtSession(context: Context): OrtSession {
        val ortEnvironment = getOrtEnvironment()
        val onnxModelBytes = readOnnxModelBytes(context)

        return OrtSession.SessionOptions().use {
            it.setIntraOpNumThreads(Runtime.getRuntime().availableProcessors() / 2)

            ortEnvironment.createSession(onnxModelBytes, it)
        }
    }

    private fun matToModelInput(rgbMat: Mat): OnnxTensor {
        val ortMat = Mat()
        Imgproc.resize(rgbMat, ortMat, imageSize)

        // convert cv.Mat into ByteBuffer
        val size = ortMat.total() * ortMat.elemSize()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(size.toInt())
        ortMat.get(0, 0, byteBuffer.array())

        return OnnxTensor.createTensor(
            getOrtEnvironment(), byteBuffer, imageShape, OnnxJavaType.UINT8
        )
    }

    private fun modelDecodedOutputToString(onnxTensor: OnnxTensor): String {
        val rawPredictions = mutableListOf<Int>()
        for (i in 0 until onnxTensor.info.shape[0]) {
            rawPredictions.add(onnxTensor.longBuffer.get(i.toInt()).toInt())
        }

        val predictions = rawPredictions.map { labels[it] }
        var lastChar: String? = null
        return buildString {
            for (char in predictions) {
                if (char == lastChar) continue
                if (char == blankToken) {
                    lastChar = null
                    continue
                }
                append(char)
                lastChar = char
            }
        }
    }

    fun ocrBgrMat(bgrMat: Mat, ortSession: OrtSession): String {
        val rgbMat = Mat()
        Imgproc.cvtColor(bgrMat, rgbMat, Imgproc.COLOR_BGR2RGB)

        val inputTensor = matToModelInput(rgbMat)
        val result = ortSession.run(mapOf("raw_image" to inputTensor))
        val decodedOutput = result.get("decoded_output")
            .getOrElse { throw NullPointerException("ONNX model output null!") }
        val finalResult = modelDecodedOutputToString(decodedOutput as OnnxTensor)
        var placeholderCount = 0
        return buildString {
            for (char in finalResult) {
                if (placeholderCount == 2) break

                if (char.toString() == padToken) {
                    placeholderCount += 1
                    continue
                } else {
                    placeholderCount = 0
                    append(char)
                }
            }
        }
    }
}
