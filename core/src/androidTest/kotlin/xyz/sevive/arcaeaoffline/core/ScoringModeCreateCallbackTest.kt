package xyz.sevive.arcaeaoffline.core

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.ScoringModeCreateCallback
import xyz.sevive.arcaeaoffline.core.database.entities.Property
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository

@RunWith(AndroidJUnit4::class)
class ScoringModeCreateCallbackTest {
    private lateinit var db: ArcaeaOfflineDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, ArcaeaOfflineDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .addCallback(ScoringModeCreateCallback)
                .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun creationStoresTheScoringMode() {
        val property = runBlocking { db.propertyDao().find(Property.KEY_SCORING_MODE).first() }

        assertEquals(PropertyRepository.DEFAULT_SCORING_MODE.key.toString(), property?.value)
    }
}
