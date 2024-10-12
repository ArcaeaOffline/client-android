package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

object ArcaeaResourcesStateHolder {
    private const val LOG_TAG = "ArcaeaResStateHolder"

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private data class Status(
        val isArcaeaInstalled: Boolean = false,
        val hasPacklist: Boolean = false,
        val hasSonglist: Boolean = false,
        val hasJackets: Boolean = false,
        val hasPartnerIcons: Boolean = false,
    )

    private val status = MutableStateFlow(Status())
    private var statusReloadJob: Job? = null

    val canImportLists = status.map { it.hasPacklist || it.hasSonglist }.stateIn(
        scope, SharingStarted.Eagerly, false
    )

    val canBuildHashesDatabase = status.map { it.hasJackets && it.hasPartnerIcons }.stateIn(
        scope, SharingStarted.Eagerly, false
    )

    fun reloadStatus(context: Context) {
        statusReloadJob?.cancel()
        statusReloadJob = scope.launch {
            val packageHelper = ArcaeaPackageHelper(context)

            status.value = Status(
                isArcaeaInstalled = packageHelper.isInstalled(),
                hasPacklist = packageHelper.getPacklistEntry() != null,
                hasSonglist = packageHelper.getSonglistEntry() != null,
                hasJackets = packageHelper.apkJacketZipEntries().isNotEmpty(),
                hasPartnerIcons = packageHelper.apkPartnerIconZipEntries().isNotEmpty(),
            )
            Log.d(LOG_TAG, "reloaded, ${status.value}")

            statusReloadJob = null
        }
    }
}
