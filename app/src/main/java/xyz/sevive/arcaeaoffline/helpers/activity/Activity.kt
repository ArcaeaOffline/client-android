package xyz.sevive.arcaeaoffline.helpers.activity

import android.app.Activity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable

fun Activity.getSourcePackageName(): String? = intent.`package` ?: referrer?.toString()?.replace("android-app://", "")

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun Activity.calculateWindowSizeClass(): WindowSizeClass = calculateWindowSizeClass(this)
