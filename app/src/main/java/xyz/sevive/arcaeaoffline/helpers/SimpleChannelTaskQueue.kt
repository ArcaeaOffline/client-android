package xyz.sevive.arcaeaoffline.helpers

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


/**
 * @param [capacity] If set to null, defaults to [Channel.RENDEZVOUS], which is different from the default value [Channel.BUFFERED].
 */
class SimpleChannelTaskQueue<T>(
    private val coroutineScope: CoroutineScope,
    private var capacity: Int? = Channel.BUFFERED,
    var parallelCount: Int = Runtime.getRuntime().availableProcessors(),
) {
    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _progress = MutableStateFlow(-1)
    val progress = _progress.asStateFlow()

    private val _progressTotal = MutableStateFlow(-1)
    val progressTotal = _progressTotal.asStateFlow()

    private val progressLock = Mutex()

    private lateinit var channel: Channel<T>

    private fun renewChannel() {
        channel = Channel(capacity ?: Channel.RENDEZVOUS)
    }

    init {
        renewChannel()
        stop()
    }

    fun logHashCode(): String {
        return Integer.toHexString(this.hashCode())
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun start(tasks: Iterable<T>, block: suspend (T) -> Unit) {
        if (isRunning.value || !channel.isClosedForReceive) {
            Log.e(
                LOG_TAG,
                "Queue ${logHashCode()}: Previous channel hasn't stopped! Start request ignored."
            )
            return
        }

        if (tasks.count() == 0) {
            Log.w(LOG_TAG, "Queue ${logHashCode()}: Task list empty! Check it before starting?")
            return
        }

        renewChannel()

        _isRunning.value = true
        _progress.value = 0
        _progressTotal.value = tasks.count()
        Log.i(
            LOG_TAG,
            "Queue ${logHashCode()} started, capacity=${capacity}, parallelCount=${parallelCount}, coroutineScope=${coroutineScope}"
        )

        runBlocking {
            launch {
                for (task in tasks) {
                    if (channel.isClosedForSend) break
                    channel.send(task)
                }
                channel.close()
            }

            launch {
                (0 until parallelCount).map {
                    coroutineScope.async {
                        for (task in channel) {
                            block(task)
                            progressLock.withLock {
                                _progress.value += 1
                            }
                        }
                    }
                }.awaitAll()
            }.invokeOnCompletion {
                Log.i(
                    LOG_TAG,
                    "Queue ${logHashCode()} stopped, progress ${progress.value}/${progressTotal.value}"
                )
                _isRunning.value = false
                _progress.value = -1
                _progressTotal.value = -1
            }
        }
    }

    fun stop(): Boolean {
        val result = channel.close()
        Log.i(LOG_TAG, "Queue ${logHashCode()}: Stop request, channel close result: $result")
        return result
    }

    companion object {
        const val LOG_TAG = "SimpleChannelTaskQueue"
    }
}
