package xyz.sevive.arcaeaoffline.ui


import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import xyz.sevive.arcaeaoffline.ArcaeaOfflineApplication
import xyz.sevive.arcaeaoffline.ui.database.DatabaseEntryViewModel
import xyz.sevive.arcaeaoffline.ui.database.DatabaseManageViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ItemEditViewModel
        initializer {
            DatabaseEntryViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabaseManageViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [ArcaeaOfflineApplication].
 */
fun CreationExtras.application(): ArcaeaOfflineApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as ArcaeaOfflineApplication)