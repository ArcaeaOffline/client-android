package xyz.sevive.arcaeaoffline.ui


import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import xyz.sevive.arcaeaoffline.ArcaeaOfflineApplication
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareViewModel
import xyz.sevive.arcaeaoffline.ui.components.ChartSelectorViewModel
import xyz.sevive.arcaeaoffline.ui.components.SongIdSelectorViewModel
import xyz.sevive.arcaeaoffline.ui.database.DatabaseAddScoreViewModel
import xyz.sevive.arcaeaoffline.ui.database.DatabaseNavEntryViewModel
import xyz.sevive.arcaeaoffline.ui.database.b30list.DatabaseB30ListViewModel
import xyz.sevive.arcaeaoffline.ui.database.manage.DatabaseManageViewModel
import xyz.sevive.arcaeaoffline.ui.database.scorelist.DatabaseScoreListViewModel
import xyz.sevive.arcaeaoffline.ui.ocr.queue.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.ui.ocr.queue.OcrQueueViewModel
import xyz.sevive.arcaeaoffline.ui.ocr.queue.ocrQueueDataStore
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
            DatabaseB30ListViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            SongIdSelectorViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            ChartSelectorViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            OcrQueueViewModel(
                OcrQueuePreferencesRepository(application().ocrQueueDataStore)
            )
        }

        initializer {
            OcrFromShareViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer,
                application().appDatabaseRepositoryContainer
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
