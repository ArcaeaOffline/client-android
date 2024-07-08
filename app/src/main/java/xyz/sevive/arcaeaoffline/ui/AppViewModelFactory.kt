package xyz.sevive.arcaeaoffline.ui


import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import xyz.sevive.arcaeaoffline.ArcaeaOfflineApplication
import xyz.sevive.arcaeaoffline.ui.activities.EmergencyModeActivityViewModel
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareViewModel
import xyz.sevive.arcaeaoffline.ui.components.ChartSelectorViewModel
import xyz.sevive.arcaeaoffline.ui.components.SongIdSelectorViewModel
import xyz.sevive.arcaeaoffline.ui.database.DatabaseAddPlayResultViewModel
import xyz.sevive.arcaeaoffline.ui.database.DatabaseNavEntryViewModel
import xyz.sevive.arcaeaoffline.ui.database.DatabasePlayResultListViewModel
import xyz.sevive.arcaeaoffline.ui.database.DatabaseR30ListViewModel
import xyz.sevive.arcaeaoffline.ui.database.b30list.DatabaseB30ListViewModel
import xyz.sevive.arcaeaoffline.ui.database.manage.DatabaseManageViewModel
import xyz.sevive.arcaeaoffline.ui.ocr.queue.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.ui.ocr.queue.OcrQueueViewModel
import xyz.sevive.arcaeaoffline.ui.ocr.queue.ocrQueueDataStore
import xyz.sevive.arcaeaoffline.ui.overview.OverviewViewModel
import java.util.Locale

/**
 * Provides Factory to create instance of ViewModel for the entire app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ViewModels
        initializer {
            EmergencyModeActivityViewModel(application().emergencyModePreferencesRepository)
        }

        initializer {
            OverviewViewModel(
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
            DatabaseAddPlayResultViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabasePlayResultListViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabaseB30ListViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer
            )
        }

        initializer {
            DatabaseR30ListViewModel(
                application().arcaeaOfflineDatabaseRepositoryContainer,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .withZone(ZoneId.systemDefault())
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
