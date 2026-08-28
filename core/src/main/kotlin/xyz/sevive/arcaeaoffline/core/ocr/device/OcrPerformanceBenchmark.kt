package xyz.sevive.arcaeaoffline.core.ocr.device

import ai.onnxruntime.OrtSession
import android.content.Context
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.opencv.core.Mat
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.DeviceRoisAutoSelector
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.DeviceRoisAutoSelectorResult
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT1
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT2
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.extractor.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.core.ocr.opencv.use
import kotlin.system.measureTimeMillis

/**
 * OCR performance benchmark. Mirrors the production queue's Channel-based
 * concurrency model (OcrQueueProcessingJob): runs fixed task batches at a
 * given parallel count and reports timings/throughput, so users can pick a
 * suitable OcrQueuePreferences.parallelCount.
 *
 * Inference path matches production (DeviceOcrOnnxHelper.createOrtSession + ocrBgrMat).
 */
object OcrPerformanceBenchmark {
    private const val LOG_TAG = "OcrPerfBench"
    private val logger = Logger.withTag(LOG_TAG)

    const val DEFAULT_TASKS_PER_BATCH: Int = 40
    const val DEFAULT_WARMUP_BATCHES: Int = 1
    const val DEFAULT_TIMED_BATCHES: Int = 5

    data class Result(
        val batchTimesMs: List<Long>,
        val medianPerImageMs: Double,
        val throughputPerSecond: Double,
        val resultsConsistent: Boolean,
    )

    /**
     * Returns the OCR ROIs for one image as cloned Mats that own their data;
     * ownership transfers to the caller.
     */
    fun extractRois(img: Mat): List<Mat> =
        CropBlackEdges.crop(img).use { imgCropped ->
            val rois =
                when (DeviceRoisAutoSelector.select(img)) {
                    DeviceRoisAutoSelectorResult.T1 -> DeviceRoisAutoT1(imgCropped.width(), imgCropped.height())
                    else -> DeviceRoisAutoT2(imgCropped.width(), imgCropped.height())
                }
            val extractor = DeviceRoisExtractor(rois, imgCropped)

            // extractor properties return submat views of imgCropped; clone them
            // so the returned Mats stay valid after imgCropped is released.
            listOf(extractor.pure, extractor.far, extractor.lost, extractor.score, extractor.maxRecall).map { view ->
                view.use { it.clone() }
            }
        }

    /**
     * Takes ownership of [roiSets]: the ROI Mats are released on every exit
     * path (completion, failure, cancellation). Reuses the same ROI Mats for
     * all batches and warmups; inference reads them without mutating, which
     * is safe concurrently.
     */
    suspend fun runBenchmark(
        context: Context,
        roiSets: List<List<Mat>>,
        parallel: Int,
        tasksPerBatch: Int = DEFAULT_TASKS_PER_BATCH,
        warmupBatches: Int = DEFAULT_WARMUP_BATCHES,
        timedBatches: Int = DEFAULT_TIMED_BATCHES,
        onProgress: (completed: Int, total: Int) -> Unit = { _, _ -> },
    ): Result {
        require(roiSets.isNotEmpty()) { "At least one image is required" }
        require(parallel >= 1) { "Parallel count must be >= 1" }

        val tasks = List(tasksPerBatch) { roiSets[it % roiSets.size] }
        try {
            DeviceOcrOnnxHelper.createOrtSession(context).use { session ->
                val totalBatches = warmupBatches + timedBatches
                repeat(warmupBatches) { i ->
                    processBatch(tasks, parallel, session)
                    onProgress(i + 1, totalBatches)
                }

                val batchTimes = mutableListOf<Long>()
                var reference: List<List<String>>? = null
                var consistent = true
                repeat(timedBatches) { i ->
                    val batch = processBatch(tasks, parallel, session)
                    batchTimes += batch.elapsedMs
                    val ref = reference ?: batch.results.also { reference = it }
                    if (batch.results != ref) consistent = false
                    onProgress(warmupBatches + i + 1, totalBatches)
                }

                // Median resists single-run spikes (DVFS/scheduling noise), used instead of the mean
                val sortedBatchTimes = batchTimes.sorted()
                val medianBatchMs = sortedBatchTimes[sortedBatchTimes.size / 2].toDouble()
                logger.i {
                    "Benchmark done: parallel %d, taskPerBatch %d, batchTimes(ms) %s, throughput %.1f it/s, consistent %s".format(
                        parallel,
                        tasksPerBatch,
                        batchTimes.joinToString("/"),
                        tasksPerBatch * 1000.0 / medianBatchMs,
                        consistent,
                    )
                }
                return Result(
                    batchTimesMs = batchTimes,
                    medianPerImageMs = medianBatchMs / tasksPerBatch,
                    throughputPerSecond = tasksPerBatch * 1000.0 / medianBatchMs,
                    resultsConsistent = consistent,
                )
            }
        } finally {
            roiSets.flatten().forEach { it.release() }
        }
    }

    private data class BatchResult(
        val elapsedMs: Long,
        val results: List<List<String>>,
    )

    private suspend fun processBatch(
        tasks: List<List<Mat>>,
        parallel: Int,
        session: OrtSession,
    ): BatchResult {
        val results = MutableList(tasks.size) { emptyList<String>() }
        val elapsedMs =
            measureTimeMillis {
                coroutineScope {
                    val channel = Channel<Int>(parallel)

                    launch {
                        tasks.indices.forEach { channel.send(it) }
                        channel.close()
                    }

                    repeat(parallel) {
                        launch(Dispatchers.IO) {
                            for (idx in channel) {
                                results[idx] = tasks[idx].map { DeviceOcrOnnxHelper.ocrBgrMat(it, session) }
                            }
                        }
                    }
                }
            }
        return BatchResult(elapsedMs, results)
    }
}
