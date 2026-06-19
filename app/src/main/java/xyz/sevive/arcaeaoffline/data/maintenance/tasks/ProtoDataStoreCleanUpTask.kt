package xyz.sevive.arcaeaoffline.data.maintenance.tasks

import android.content.Context
import androidx.datastore.dataStoreFile
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.data.maintenance.AppDataMaintenanceTask

class ProtoDataStoreCleanUpTask(
    context: Context,
) : AppDataMaintenanceTask {
    override val id = "proto_datastore_cleanup"
    override val version = 1

    private val logger = Logger.withTag("ProtoDataStoreCleanUpTask")

    private val legacyFiles =
        listOf(
            context.dataStoreFile("app_prefs.preferences_pb"),
            context.dataStoreFile("emergency_mode_activity_prefs.pb.preferences_pb"),
            context.dataStoreFile("ocr_queue_prefs.pb.preferences_pb"),
            context.dataStoreFile("unstable_flavor_prefs.preferences_pb"),
        ).map { PlatformFile(it) }

    private suspend fun executeTask() {
        val files = legacyFiles.filter { it.exists() }
        if (files.isEmpty()) return

        files.forEach {
            it.delete()
            logger.i { "Deleted file: ${it.path}" }
        }
    }

    override suspend fun execute() =
        withContext(Dispatchers.IO) {
            executeTask()
        }
}
