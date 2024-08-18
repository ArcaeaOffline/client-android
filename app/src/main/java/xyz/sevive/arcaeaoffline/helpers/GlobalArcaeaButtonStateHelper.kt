package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaButtonState
import kotlin.coroutines.CoroutineContext

object GlobalArcaeaButtonStateHelper {
    private const val LOG_TAG = "ArcaeaButtonStateHelper"

    private var isArcaeaInstalled = false

    private var hasPacklist = false
    private var hasSonglist = false

    private var hasJackets = false
    private var hasPartnerIcons = false

    private val _packlistButtonState = MutableStateFlow(ArcaeaButtonState.NOT_INSTALLED)
    val packlistButtonState = _packlistButtonState.asStateFlow()

    private val _songlistButtonState = MutableStateFlow(ArcaeaButtonState.NOT_INSTALLED)
    val songlistButtonState = _songlistButtonState.asStateFlow()

    private val _importListsFromApkButtonState = MutableStateFlow(ArcaeaButtonState.NOT_INSTALLED)
    val importListsFromApkButtonState = _importListsFromApkButtonState.asStateFlow()

    private val _buildHashesDatabaseButtonState = MutableStateFlow(ArcaeaButtonState.NOT_INSTALLED)
    val buildHashesDatabaseButtonState = _buildHashesDatabaseButtonState.asStateFlow()

    suspend fun reload(
        context: Context,
        coroutineContext: CoroutineContext = Dispatchers.IO,
    ) {
        reloadVariables(context, coroutineContext)
        Log.d(
            LOG_TAG,
            "reloaded, isArcaeaInstalled: $isArcaeaInstalled, hasPacklist: $hasPacklist, hasSonglist: $hasSonglist, hasJackets: $hasJackets, hasPartnerIcons: $hasPartnerIcons"
        )
        reloadButtonStates()
    }

    private suspend fun reloadVariables(
        context: Context,
        coroutineContext: CoroutineContext = Dispatchers.IO
    ) {
        withContext(coroutineContext) {
            launch {
                val packageHelper = ArcaeaPackageHelper(context)

                isArcaeaInstalled = packageHelper.isInstalled()
                hasPacklist = packageHelper.getPacklistEntry() != null
                hasSonglist = packageHelper.getSonglistEntry() != null
                hasJackets = packageHelper.apkJacketZipEntries().isNotEmpty()
                hasPartnerIcons = packageHelper.apkPartnerIconZipEntries().isNotEmpty()
            }
        }
    }

    private suspend fun reloadButtonStates(coroutineContext: CoroutineContext = Dispatchers.Main) {
        withContext(coroutineContext) {
            launch {
                _packlistButtonState.value = packlistButtonState()
                _songlistButtonState.value = songlistButtonState()
                _importListsFromApkButtonState.value = importListsFromApkButtonState()
                _buildHashesDatabaseButtonState.value = buildHashesDatabaseButtonState()
            }
        }
    }

    private fun packlistButtonState(): ArcaeaButtonState {
        return when {
            !isArcaeaInstalled -> ArcaeaButtonState.NOT_INSTALLED
            hasPacklist -> ArcaeaButtonState.NORMAL
            else -> ArcaeaButtonState.RESOURCE_UNAVAILABLE
        }
    }

    private fun songlistButtonState(): ArcaeaButtonState {
        return when {
            !isArcaeaInstalled -> ArcaeaButtonState.NOT_INSTALLED
            hasSonglist -> ArcaeaButtonState.NORMAL
            else -> ArcaeaButtonState.RESOURCE_UNAVAILABLE
        }
    }

    private fun importListsFromApkButtonState(): ArcaeaButtonState {
        return when {
            !isArcaeaInstalled -> ArcaeaButtonState.NOT_INSTALLED
            hasPacklist && hasSonglist -> ArcaeaButtonState.NORMAL
            else -> ArcaeaButtonState.RESOURCE_UNAVAILABLE
        }
    }

    private fun buildHashesDatabaseButtonState(): ArcaeaButtonState {
        return when {
            !isArcaeaInstalled -> ArcaeaButtonState.NOT_INSTALLED
            hasJackets && hasPartnerIcons -> ArcaeaButtonState.NORMAL
            else -> ArcaeaButtonState.RESOURCE_UNAVAILABLE
        }
    }
}
