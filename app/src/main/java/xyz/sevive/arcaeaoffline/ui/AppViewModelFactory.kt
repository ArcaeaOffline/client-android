package xyz.sevive.arcaeaoffline.ui


import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import xyz.sevive.arcaeaoffline.ArcaeaOfflineApplication
import xyz.sevive.arcaeaoffline.OcrFromShareViewModel
import xyz.sevive.arcaeaoffline.ui.components.SongIdSelectorViewModel
import xyz.sevive.arcaeaoffline.ui.database.DatabaseAddScoreViewModel
import xyz.sevive.arcaeaoffline.ui.database.DatabaseNavEntryViewModel
import xyz.sevive.arcaeaoffline.ui.database.manage.DatabaseManageViewModel
import xyz.sevive.arcaeaoffline.ui.database.scorelist.DatabaseScoreListViewModel
import xyz.sevive.arcaeaoffline.ui.models.DatabaseCommonFunctionsViewModel
import xyz.sevive.arcaeaoffline.ui.overview.OverviewModel

/**
 * Provides Factory to create instance of ViewModel for the entire app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ViewModels
        initializer {
            OverviewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabaseNavEntryViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabaseManageViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabaseAddScoreViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabaseScoreListViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabaseCommonFunctionsViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            SongIdSelectorViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            OcrFromShareViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer,
                application().appDatabase
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
