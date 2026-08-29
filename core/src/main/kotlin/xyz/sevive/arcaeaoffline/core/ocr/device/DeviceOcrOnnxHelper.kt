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
import xyz.sevive.arcaeaoffline.core.ocr.opencv.use
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

    private fun readOnnxModelBytes(context: Context): ByteArray = context.assets.open(MODEL_ASSET_PATH).use { it.readBytes() }

    /**
     * Verifies the bundled model asset against model_info.json. Creating the
     * ORT session validates the file itself and the custom ORT build's op
     * coverage; the session metadata is then cross-checked against the json
     * description. Returns the parsed info file on success; throws
     * [IllegalStateException] listing all mismatches otherwise.
     */
    fun verifyModelAsset(context: Context): ModelInfoFile {
        val infoFile = loadModelInfoFile(context)

        createOrtSession(context).use { session ->
            val metadata = session.metadata
            val mismatches =
                collectMetadataMismatches(
                    modelVersion = metadata.version,
                    producerName = metadata.producerName,
                    domain = metadata.domain,
                    graphName = metadata.graphName,
                    customMetadata = metadata.customMetadata,
                    inputNames = session.inputNames,
                    outputNames = session.outputNames,
                    infoFile = infoFile,
                )

            if (mismatches.isNotEmpty()) {
                throw IllegalStateException(
                    "OCR model asset does not match model_info.json:\n" + mismatches.joinToString("\n"),
                )
            }
        }

        return infoFile
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

/**
 * Cross-checks the parsed model_info.json against the model asset's
 * metadata. Returns one message per mismatch; empty when they match.
 *
 * Pure function (no Android or ORT types in the signature) so mismatch
 * scenarios can be unit-tested on the JVM.
 */
internal fun collectMetadataMismatches(
    modelVersion: Long,
    producerName: String?,
    domain: String?,
    graphName: String?,
    customMetadata: Map<String, String>,
    inputNames: Set<String>,
    outputNames: Set<String>,
    infoFile: DeviceOcrOnnxHelper.ModelInfoFile,
): List<String> {
    val mismatches = mutableListOf<String>()

    val patch = infoFile.patch
    if (patch == null) {
        mismatches += "model_info.json is missing the `patch` block"
        return mismatches
    }

    val modelVersionList = onnxModelVersion(modelVersion)
    if (patch.version != modelVersionList) {
        mismatches += "version: json ${patch.version}, model $modelVersionList"
    }

    if (patch.producerName != producerName) {
        mismatches += "producer_name: json ${patch.producerName}, model $producerName"
    }
    if (patch.domain != domain) {
        mismatches += "domain: json ${patch.domain}, model $domain"
    }
    if (patch.graphName != graphName) {
        mismatches += "graph_name: json ${patch.graphName}, model $graphName"
    }

    if (patch.inputNames.toSet() != inputNames) {
        mismatches += "input_names: json ${patch.inputNames}, model $inputNames"
    }
    if (patch.outputNames.toSet() != outputNames) {
        mismatches += "output_names: json ${patch.outputNames}, model $outputNames"
    }

    val training = infoFile.training
    mismatches +=
        listOfNotNull(
            customFieldMismatch(customMetadata, "image_width", training.imageWidth.toString()),
            customFieldMismatch(customMetadata, "image_height", training.imageHeight.toString()),
            customFieldMismatch(customMetadata, "blank_token", training.blankToken),
            customFieldMismatch(customMetadata, "pad_token", training.padToken),
            // 0 is the "not written" marker on the json side, matching an absent key
            customTimestampMismatch(customMetadata, "built_timestamp", training.builtTimestamp),
            customTimestampMismatch(customMetadata, "patched_timestamp", patch.patchedTimestamp),
        )

    return mismatches
}

private fun customFieldMismatch(
    customMetadata: Map<String, String>,
    key: String,
    jsonValue: String,
): String? {
    val modelValue = customMetadata[key]
    return when {
        modelValue == null -> "`$key`: json has \"$jsonValue\" but missing in model metadata"
        modelValue != jsonValue -> "`$key`: json \"$jsonValue\", model \"$modelValue\""
        else -> null
    }
}

private fun customTimestampMismatch(
    customMetadata: Map<String, String>,
    key: String,
    jsonValue: Long,
): String? {
    val modelValue = customMetadata[key]
    return when {
        jsonValue == 0L && modelValue == null -> null
        jsonValue == 0L -> "`$key`: missing in json but model has \"$modelValue\""
        modelValue == null -> "`$key`: json has $jsonValue but missing in model metadata"
        modelValue.toLongOrNull() != jsonValue -> "`$key`: json $jsonValue, model \"$modelValue\""
        else -> null
    }
}

/**
 * @see <a href="https://onnx.ai/onnx/repo-docs/Versioning.html#serializing-semver-version-numbers-in-protobuf">ONNX documentation</a>
 */
private fun onnxModelVersion(version: Long): List<Int> {
    val major = ((version shr 48) and 0xFFFF).toInt()
    val minor = ((version shr 32) and 0xFFFF).toInt()
    val patch = (version and 0xFFFFFFFF).toInt()
    return listOf(major, minor, patch)
}
