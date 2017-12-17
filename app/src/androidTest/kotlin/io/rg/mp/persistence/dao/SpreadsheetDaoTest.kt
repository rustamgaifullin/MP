package io.rg.mp.persistence.dao

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import io.reactivex.subscribers.TestSubscriber
import io.rg.mp.persistence.Database
import io.rg.mp.persistence.entity.Spreadsheet
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@SmallTest
@RunWith(AndroidJUnit4::class)
class SpreadsheetDaoTest {
    private lateinit var spreadsheetDao: SpreadsheetDao
    private lateinit var db: Database
    private lateinit var testSubscriber: TestSubscriber<List<String>>

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Room.inMemoryDatabaseBuilder(context, Database::class.java).build()
        spreadsheetDao = db.spreadsheetDao()

        testSubscriber = TestSubscriber()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun shouldFindRecordsForUpdate() {
        val ids = listOf("1", "2", "3", "4")
        val names = listOf("name12", "name22")
        prepareTestData()

        spreadsheetDao.findRecordsForUpdate(ids, names)
                .toFlowable()
                .subscribe(testSubscriber)

        testSubscriber.assertValue {
            it.contains("1") && it.contains("2") && it.size == 2
        }

    }

    @Test
    fun shouldFindRecordsForDelete() {
        val ids = listOf("1", "2", "7")
        prepareTestData()

        spreadsheetDao.findRecordsForDelete(ids)
                .toFlowable()
                .subscribe(testSubscriber)

        testSubscriber.assertValue {
            it.contains("5") && it.contains("6") && it.size == 2
        }
    }
    
    private fun prepareTestData() {
        val spreadsheetArray = arrayOf(
                Spreadsheet("1", "name1", 123),
                Spreadsheet("2", "name2", 123),
                Spreadsheet("5", "name5", 123),
                Spreadsheet("6", "name6", 123),
                Spreadsheet("7", "name7", 123)
        )

        spreadsheetDao.insertAll(*spreadsheetArray)
    }
}