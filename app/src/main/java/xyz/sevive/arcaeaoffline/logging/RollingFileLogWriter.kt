/*
 * Copyright (c) 2024 Touchlab
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Vendored from co.touchlab:kermit-io (github.com/touchlab/Kermit, Apache-2.0).
 * See the note in RollingFileLogWriterConfig.kt.
 */

package xyz.sevive.arcaeaoffline.logging

import co.touchlab.kermit.DefaultFormatter
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlin.math.max
import kotlin.time.Clock

/**
 * Implements a log writer that writes log messages to a rolling file.
 *
 * It also deletes old log files when the maximum number of log files is reached. We simply keep approximately
 * [RollingFileLogWriterConfig.rollOnSize] bytes in each log file, and delete the oldest file when we have more than
 * [RollingFileLogWriterConfig.maxLogFiles].
 *
 * Formatting is governed by the passed [MessageStringFormatter], but we do prepend a timestamp by default. Turn this off via
 * [RollingFileLogWriterConfig.prependTimestamp]
 *
 * Writes to the file are done by a different coroutine. The main reason for this is to make writes to the log file sink thread-safe, and
 * so that file rolling can be performed without additional synchronization or locking. Log messages reach that coroutine through a channel
 * that buffers up to [LOGGING_CHANNEL_CAPACITY] of them, so logging threads normally hand a message off and return without waiting for the
 * I/O. Once that buffer is full, logging threads block until the writer catches up, which keeps memory bounded and limits how many messages
 * can be lost if the process dies. Threads that have already been interrupted are never blocked -- see [sendBlockingUnlessInterrupted].
 */
open class RollingFileLogWriter(
    private val config: RollingFileLogWriterConfig,
    private val clock: Clock,
    private val messageStringFormatter: MessageStringFormatter = DefaultFormatter,
    private val fileSystem: FileSystem = SystemFileSystem,
) : LogWriter() {
    constructor(
        config: RollingFileLogWriterConfig,
        messageStringFormatter: MessageStringFormatter = DefaultFormatter,
        fileSystem: FileSystem = SystemFileSystem,
    ) : this(config, Clock.System, messageStringFormatter, fileSystem)

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val coroutineScope =
        CoroutineScope(
            newSingleThreadContext("RollingFileLogWriter") +
                SupervisorJob() +
                CoroutineName("RollingFileLogWriter") +
                CoroutineExceptionHandler { _, throwable ->
                    // can't log it, we're the logger -- print to standard error
                    println("RollingFileLogWriter: Uncaught exception in writer coroutine")
                    throwable.printStackTrace()
                },
        )

    private val loggingChannel: Channel<Buffer> = Channel(capacity = LOGGING_CHANNEL_CAPACITY)

    init {
        coroutineScope.launch {
            writer()
        }
    }

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        bufferLog(
            formatMessage(
                severity = severity,
                tag = Tag(tag),
                message = Message(message),
            ),
            throwable,
        )
    }

    private fun bufferLog(
        message: String,
        throwable: Throwable?,
    ) {
        val log =
            buildString {
                if (config.prependTimestamp) {
                    append(clock.now().format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET))
                    append(" ")
                }
                appendLine(message)
                if (throwable != null) {
                    appendLine(throwable.stackTraceToString())
                }
            }
        loggingChannel.sendBlockingUnlessInterrupted(Buffer().apply { writeString(log) })
    }

    private fun formatMessage(
        severity: Severity,
        tag: Tag?,
        message: Message,
    ): String = messageStringFormatter.formatMessage(severity, if (config.logTag) tag else null, message)

    private fun shouldRollLogs(
        currentSize: Long,
        logFilePath: Path,
    ): Boolean = max(currentSize, fileSizeOrZero(logFilePath)) > config.rollOnSize

    private fun rollLogs() {
        if (fileSystem.exists(pathForLogIndex(config.maxLogFiles - 1))) {
            fileSystem.delete(pathForLogIndex(config.maxLogFiles - 1))
        }
        (0..<(config.maxLogFiles - 1)).reversed().forEach {
            val sourcePath = pathForLogIndex(it)
            val targetPath = pathForLogIndex(it + 1)
            if (fileSystem.exists(sourcePath)) {
                try {
                    fileSystem.atomicMove(sourcePath, targetPath)
                } catch (e: IOException) {
                    // we can't log it, we're the logger -- print to standard error
                    println(
                        "RollingFileLogWriter: Failed to roll log file $sourcePath to $targetPath (sourcePath exists=${
                            fileSystem.exists(
                                sourcePath,
                            )
                        })",
                    )
                    e.printStackTrace()
                }
            }
        }
    }

    private fun pathForLogIndex(index: Int): Path {
        // kotlinx-io 0.9 has no Path(parent, child) constructor; join manually.
        val fileName =
            if (index == 0) "${config.logFileName}.log" else "${config.logFileName}-$index.log"
        return Path("${config.logFilePath}/$fileName")
    }

    private suspend fun writer() {
        val logFilePath = pathForLogIndex(0)

        fun createNewLogSink(): Sink =
            fileSystem
                .sink(logFilePath, append = true)
                .buffered()

        var currentLogSink: Sink? = null
        // Track file size internally to avoid relying on filesystem metadata, which can be
        // stale on Windows while a write handle is open.
        var currentFileSize = fileSizeOrZero(logFilePath)

        // Tracks whether we are currently in an error state to avoid spamming on every write
        var ioErrorActive = false

        while (currentCoroutineContext().isActive) {
            // wait for data to be available, flush periodically
            val result = loggingChannel.receiveCatching()

            try {
                // check if logs need rolling
                if (shouldRollLogs(currentFileSize, logFilePath)) {
                    currentLogSink?.close()
                    rollLogs()
                    currentLogSink = createNewLogSink()
                    currentFileSize = 0
                }

                if (currentLogSink == null) {
                    currentLogSink = createNewLogSink()
                }

                val data = result.getOrNull()
                val bytesWritten = data?.size ?: 0
                data?.transferTo(currentLogSink)
                currentFileSize += bytesWritten

                // we could improve performance by flushing less frequently at the cost of potential data loss,
                // but this is a safe default
                currentLogSink.flush()

                if (ioErrorActive) {
                    println("RollingFileLogWriter: Log file access restored")
                    ioErrorActive = false
                }
            } catch (e: IOException) {
                if (!ioErrorActive) {
                    println("RollingFileLogWriter: IOException writing to log file, some logs may be lost: ${e.message}")
                    e.printStackTrace()
                    ioErrorActive = true
                }
                try {
                    currentLogSink?.close()
                } catch (_: IOException) {
                    // ignore close failure
                }
                currentLogSink = null
                currentFileSize = fileSizeOrZero(logFilePath)
            }
        }

        currentLogSink?.close()
    }

    private fun fileSizeOrZero(path: Path) = fileSystem.metadataOrNull(path)?.size ?: 0
}

/**
 * How many log messages [RollingFileLogWriter] buffers before logging threads have to wait for the writer coroutine to catch up.
 */
internal const val LOGGING_CHANNEL_CAPACITY = 64
