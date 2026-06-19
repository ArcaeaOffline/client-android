package xyz.sevive.arcaeaoffline.data.maintenance

import android.content.Context
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.data.maintenance.tasks.ProtoDataStoreCleanUpTask

class AppDataMaintenanceManager(
    context: Context,
) {
    private val logger = Logger.withTag("AppDataMaintenanceManager")

    private val tasks =
        listOf(
            ProtoDataStoreCleanUpTask(context),
        )

    suspend fun runAllTasks() =
        withContext(Dispatchers.Default) {
            tasks.forEach { task ->
                try {
                    logger.d { "Executing task: ${task.id} (${task.version})" }
                    task.execute()
                } catch (e: Exception) {
                    logger.e(e) { "Error executing task: ${task.id}" }
                }
            }
        }
}
