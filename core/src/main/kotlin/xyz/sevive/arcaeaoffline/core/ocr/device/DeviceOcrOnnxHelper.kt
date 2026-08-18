package xyz.sevive.arcaeaoffline.core.ocr.device

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.annotation.SuppressLint
import android.content.Context
import co.touchlab.kermit.Logger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import xyz.sevive.arcaeaoffline.core.ocr.use
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.jvm.optionals.getOrElse
import kotlin.properties.Delegates

object DeviceOcrOnnxHelper {
    private const val LOG_TAG = "OnnxHelper"
    private const val MODEL_ASSET_PATH = "ocr/model_patched.onnx"
    private val logger = Logger.withTag(LOG_TAG)

    private var imageSize by Delegates.notNull<Size>()
    private var imageShape by Delegates.notNull<LongArray>()

    private lateinit var labels: List<String>
    private lateinit var blankToken: String
    private lateinit var padToken: String

    private val jsonSerializer = Json { ignoreUnknownKeys = true }

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class ModelInfo(
        @SerialName("image_height") val imageHeight: Long,
        @SerialName("image_width") val imageWidth: Long,
        @SerialName("labels") val labels: List<String>,
        @SerialName("blank_token") val blankToken: String,
        @SerialName("pad_token") val padToken: String,
        @SerialName("built_timestamp") val builtTimestamp: Long = 0,
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class ModelInfoFile(
        val training: ModelInfo,
        val patch: ModelPatchInfo? = null,
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    data class ModelPatchInfo(
        @SerialName("version") val version: List<Int> = emptyList(),
        @SerialName("producer_name") val producerName: String? = null,
        @SerialName("producer_version") val producerVersion: String? = null,
        @SerialName("domain") val domain: String? = null,
        @SerialName("graph_name") val graphName: String? = null,
        @SerialName("input_names") val inputNames: List<String> = emptyList(),
        @SerialName("output_names") val outputNames: List<String> = emptyList(),
        @SerialName("patched_timestamp") val patchedTimestamp: Long = 0,
    )

    private fun getOrtEnvironment(): OrtEnvironment = OrtEnvironment.getEnvironment("ocr")

    fun loadModelInfoFile(context: Context): ModelInfoFile =
        jsonSerializer.decodeFromString<ModelInfoFile>(
            context.assets
                .open("ocr/model_info.json")
                .bufferedReader()
                .use { it.readText() },
        )

    fun loadModelInfo(context: Context) {
        val modelInfo = loadModelInfoFile(context).training

        logger.d { "Loaded model info $modelInfo" }

        imageSize = Size(modelInfo.imageWidth.toDouble(), modelInfo.imageHeight.toDouble())
        imageShape = longArrayOf(modelInfo.imageHeight, modelInfo.imageWidth, 3L)
        labels = modelInfo.labels.toList() // make a copy
        blankToken = modelInfo.blankToken
        padToken = modelInfo.padToken
    }

    private fun readOnnxModelBytes(context: Context): ByteArray = context.assets.open(MODEL_ASSET_PATH).readBytes()

    /**
     * Lightweight sanity check for the bundled model asset. Does not load or
     * validate the model itself (a truncated file still passes); use
     * [createOrtSession] for a full check.
     */
    fun checkModelAsset(context: Context) {
        context.assets.open(MODEL_ASSET_PATH).use { stream ->
            if (stream.read(ByteArray(1)) == -1) throw IOException("OCR model asset is empty: $MODEL_ASSET_PATH")
        }
    }

    /**
     * @see <a href="https://onnx.ai/onnx/repo-docs/Versioning.html#serializing-semver-version-numbers-in-protobuf">ONNX documentation</a>
     *
     * @return arrayOf(major, minor, patch)
     */
    @Suppress("UNUSED")
    private fun modelVersion(version: Long): List<Int> {
        val major = ((version shr 48) and 0xFFFF).toInt()
        val minor = ((version shr 32) and 0xFFFF).toInt()
        val patch = (version and 0xFFFFFFFF).toInt()
        return listOf(major, minor, patch)
    }

    fun createOrtSession(context: Context): OrtSession {
        val ortEnvironment = getOrtEnvironment()
        val onnxModelBytes = readOnnxModelBytes(context)

        return OrtSession.SessionOptions().use {
            it.setIntraOpNumThreads(Runtime.getRuntime().availableProcessors() / 2)
            // Custom build onnxruntime cannot ensure optimized node exists,
            // so the optimization must be disabled, otherwise ORT_NOT_IMPLEMENTED may occur.
            it.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.NO_OPT)

            ortEnvironment.createSession(onnxModelBytes, it)
        }
    }

    private fun matToModelInput(rgbMat: Mat): OnnxTensor =
        Mat().use { ortMat ->
            Imgproc.resize(rgbMat, ortMat, imageSize)

            // convert cv.Mat into ByteBuffer
            val size = ortMat.total() * ortMat.elemSize()
            val byteBuffer: ByteBuffer = ByteBuffer.allocate(size.toInt())
            ortMat.get(0, 0, byteBuffer.array())

            OnnxTensor.createTensor(
                getOrtEnvironment(),
                byteBuffer,
                imageShape,
                OnnxJavaType.UINT8,
            )
        }

    private fun modelDecodedOutputToString(onnxTensor: OnnxTensor): String {
        val rawPredictions = mutableListOf<Int>()
        for (i in 0 until onnxTensor.info.shape[0]) {
            rawPredictions.add(onnxTensor.intBuffer.get(i.toInt()))
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

    fun ocrBgrMat(
        bgrMat: Mat,
        ortSession: OrtSession,
    ): String {
        // Ownership notes (per ORT Java API):
        // - the input OnnxTensor is NOT owned by OrtSession.Result and must be
        //   closed by the caller;
        // - Result.close() owns and closes the output tensors it contains, and
        //   the decoded output must be read before Result closes.
        val finalResult =
            Mat().use { rgbMat ->
                Imgproc.cvtColor(bgrMat, rgbMat, Imgproc.COLOR_BGR2RGB)

                matToModelInput(rgbMat).use { inputTensor ->
                    ortSession.run(mapOf("raw_image" to inputTensor)).use { result ->
                        val decodedOutput =
                            result
                                .get("decoded_output")
                                .getOrElse { throw NullPointerException("ONNX model output null!") }
                        modelDecodedOutputToString(decodedOutput as OnnxTensor)
                    }
                }
            }
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
