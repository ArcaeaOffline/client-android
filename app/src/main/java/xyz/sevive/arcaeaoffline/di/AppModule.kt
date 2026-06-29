package xyz.sevive.arcaeaoffline.di

import android.content.Context
import com.github.panpf.sketch.Sketch
import com.github.panpf.sketch.cache.MemoryCache
import com.github.panpf.sketch.util.Logger
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.koin.plugin.module.dsl.worker
import xyz.sevive.arcaeaoffline.core.di.coreModule
import xyz.sevive.arcaeaoffline.database.AppDatabase
import xyz.sevive.arcaeaoffline.database.OcrQueueDatabase
import xyz.sevive.arcaeaoffline.database.daos.OcrHistoryDao
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueStagingBatchDao
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueStagingItemDao
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueTaskDao
import xyz.sevive.arcaeaoffline.database.repositories.OcrHistoryRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueStagingBatchRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueStagingItemRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueTaskRepositoryImpl
import xyz.sevive.arcaeaoffline.datastore.AppPreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.EmergencyModePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.UnstableFlavorPreferencesRepository
import xyz.sevive.arcaeaoffline.jobs.ImageHashesDatabaseBuilderJob
import xyz.sevive.arcaeaoffline.jobs.OcrQueueProcessingJob
import xyz.sevive.arcaeaoffline.jobs.OcrQueueStagingJob
import xyz.sevive.arcaeaoffline.jobs.R30UpdateJob
import xyz.sevive.arcaeaoffline.ui.activities.EmergencyModeActivityViewModel
import xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare.OcrFromShareViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.DatabaseNavEntryViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.addplayresult.DatabaseAddPlayResultViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.b30list.DatabaseB30ListViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.deduplicator.DatabaseDeduplicatorViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.manage.DatabaseManageViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.DatabasePlayResultListViewModel
import xyz.sevive.arcaeaoffline.ui.screens.database.r30list.DatabaseR30ListViewModel
import xyz.sevive.arcaeaoffline.ui.screens.ocr.dependencies.OcrDependenciesScreenViewModel
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.OcrQueueScreenViewModel
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.preferences.OcrQueuePreferencesViewModel
import xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.staging.OcrQueueStagingViewModel
import xyz.sevive.arcaeaoffline.ui.screens.overview.OverviewViewModel
import xyz.sevive.arcaeaoffline.ui.screens.settings.SettingsViewModel
import xyz.sevive.arcaeaoffline.ui.screens.settings.unstablealert.SettingsUnstableAlertScreenViewModel
import xyz.sevive.arcaeaoffline.ui.screens.utilities.UtilitiesChartRecommendScreenViewModel

internal fun createAppDatabase(context: Context): AppDatabase = AppDatabase.getDatabase(context)

internal fun ocrHistoryDao(db: AppDatabase) = db.ocrHistoryDao()

internal fun createOcrQueueDatabase(context: Context): OcrQueueDatabase = OcrQueueDatabase.getDatabase(context)

internal fun ocrQueueTaskDao(db: OcrQueueDatabase) = db.ocrQueueTaskDao()

internal fun ocrQueueStagingItemDao(db: OcrQueueDatabase) = db.ocrQueueStagingItemDao()

internal fun ocrQueueStagingBatchDao(db: OcrQueueDatabase) = db.ocrQueueStagingBatchDao()

val thirdPartyModule =
    module {
        single<Sketch> {
            Sketch
                .Builder(get())
                .apply {
                    logger(level = Logger.Level.Debug)
                    memoryCache {
                        MemoryCache.Builder(get()).apply { maxSizePercent(0.1) }.build()
                    }
                }.build()
        }
    }

val appModule =
    module {
        includes(coreModule)
        includes(thirdPartyModule)

        single<AppDatabase> { create(::createAppDatabase) }
        single<OcrHistoryDao> { create(::ocrHistoryDao) }
        single<OcrHistoryRepository>()

        single<OcrQueueDatabase> { create(::createOcrQueueDatabase) }
        single<OcrQueueTaskDao> { create(::ocrQueueTaskDao) }
        single<OcrQueueStagingItemDao> { create(::ocrQueueStagingItemDao) }
        single<OcrQueueStagingBatchDao> { create(::ocrQueueStagingBatchDao) }
        single<OcrQueueTaskRepositoryImpl>().bind(OcrQueueTaskRepository::class)
        single<OcrQueueStagingItemRepository>()
        single<OcrQueueStagingBatchRepository>()

        single<AppPreferencesRepository>()
        single<EmergencyModePreferencesRepository>()
        single<OcrQueuePreferencesRepository>()
        single<UnstableFlavorPreferencesRepository>()

        viewModel<EmergencyModeActivityViewModel>()
        viewModel<OverviewViewModel>()
        viewModel<DatabaseNavEntryViewModel>()
        viewModel<DatabaseManageViewModel>()
        viewModel<DatabaseAddPlayResultViewModel>()
        viewModel<DatabasePlayResultListViewModel>()
        viewModel<DatabaseB30ListViewModel>()
        viewModel<DatabaseR30ListViewModel>()
        viewModel<DatabaseDeduplicatorViewModel>()
        viewModel<OcrDependenciesScreenViewModel>()
        viewModel<OcrQueueScreenViewModel>()
        viewModel<OcrQueueStagingViewModel>()
        viewModel<OcrQueuePreferencesViewModel>()
        viewModel<OcrFromShareViewModel>()
        viewModel<SettingsViewModel>()
        viewModel<SettingsUnstableAlertScreenViewModel>()
        viewModel<UtilitiesChartRecommendScreenViewModel>()

        worker<R30UpdateJob>()
        worker<OcrQueueProcessingJob>()
        worker<ImageHashesDatabaseBuilderJob>()
        worker<OcrQueueStagingJob>()
    }
