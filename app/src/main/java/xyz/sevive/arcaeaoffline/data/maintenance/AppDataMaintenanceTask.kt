package xyz.sevive.arcaeaoffline.data.maintenance

interface AppDataMaintenanceTask {
    val id: String
    val version: Int

    suspend fun execute()
}
