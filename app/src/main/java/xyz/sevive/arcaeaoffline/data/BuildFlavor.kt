package xyz.sevive.arcaeaoffline.data

import xyz.sevive.arcaeaoffline.BuildConfig

object BuildFlavor {
    fun isUnstable(): Boolean = BuildConfig.FLAVOR == "unstable"
}

val IS_UNSTABLE_VERSION = BuildFlavor.isUnstable()
