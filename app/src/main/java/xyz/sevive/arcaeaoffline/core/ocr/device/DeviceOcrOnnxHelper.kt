package xyz.sevive.arcaeaoffline.core.ocr.device

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import org.apache.commons.io.IOUtils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.jvm.optionals.getOrElse

object DeviceOcrOnnxHelper {
    private lateinit var labelList: List<String>

    private fun getOrtEnvironment(): OrtEnvironment {
        return OrtEnvironment.getEnvironment("ocr")
    }

    fun loadLabels(context: Context) {
        labelList = IOUtils.toString(context.assets.open("ocr/labels.txt")).split("\n")
            .filter { it.isNotEmpty() }
        Log.d("OCR", labelList.toString())
    }

    private fun readOnnxModelBytes(context: Context): ByteArray {
        return IOUtils.toByteArray(context.assets.open("ocr/model_patched.onnx"))
    }

    /**
     * @see <a href="https://onnx.ai/onnx/repo-docs/Versioning.html#serializing-semver-version-numbers-in-protobuf">ONNX documentation</a>
     *
     * @return arrayOf(major, minor, patch)
     */
    fun modelVersion(ortSession: OrtSession): IntArray {
        val input = ortSession.metadata.version
        val buffer = ByteBuffer.allocate(Long.SIZE_BYTES).order(ByteOrder.BIG_ENDIAN)
        buffer.putLong(input)
        buffer.flip()

        val major = buffer.short.toInt() shl 16
        val minor = buffer.short.toInt() shl 8
        val patch = buffer.int

        return intArrayOf(major shr 16, minor shr 8, patch)
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
        Imgproc.resize(rgbMat, ortMat, Size(250.0, 50.0))

        // convert cv.Mat into ByteBuffer
        val size = ortMat.total() * ortMat.elemSize()
        val byteBuffer: ByteBuffer = ByteBuffer.allocate(size.toInt())
        ortMat.get(0, 0, byteBuffer.array())

        return OnnxTensor.createTensor(
            getOrtEnvironment(),
            byteBuffer,
            longArrayOf(50, 250, 3),
            OnnxJavaType.UINT8
        )
    }

    private fun modelDecodedOutputToString(onnxTensor: OnnxTensor): String {
        val rawPredictions = mutableListOf<Int>()
        for (i in 0 until onnxTensor.info.shape[0]) {
            rawPredictions.add(onnxTensor.longBuffer.get(i.toInt()).toInt())
        }

        val predictions = rawPredictions.map { labelList[it] }
        var lastChar: String? = null
        return buildString {
            for (char in predictions) {
                if (char == lastChar) continue
                if (char == "∅") {
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

                if (char == '-') {
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
