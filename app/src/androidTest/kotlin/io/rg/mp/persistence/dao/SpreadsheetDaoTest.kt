package io.rg.mp.persistence.dao

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import io.reactivex.subscribers.TestSubscriber
import io.rg.mp.persistence.Database
import io.rg.mp.persistence.entity.Spreadsheet
import junit.framework.Assert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@SmallTest
@RunWith(AndroidJUnit4::class)
class SpreadsheetDaoTest {
    @JvmField
    @Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var spreadsheetDao: SpreadsheetDao
    private lateinit var db: Database

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Room.inMemoryDatabaseBuilder(context, Database::class.java)
                .allowMainThreadQueries()
                .build()
        spreadsheetDao = db.spreadsheetDao()
        prepareTestData()
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

        val result = spreadsheetDao.findRecordsForUpdate(ids, names)


        Assert.assertEquals(result, listOf("1", "2"))
    }

    @Test
    fun shouldFindRecordsForDelete() {
        val ids = listOf("1", "2", "7")

        val result = spreadsheetDao.findRecordsForDelete(ids)

        Assert.assertEquals(result, listOf("5", "6"))
    }

    @Test
    fun shouldUpdateData() {
        val testSubscriber = TestSubscriber<List<Spreadsheet>>()
        val ids = listOf("1", "2", "3", "4")
        val spreadsheets = ids.map {
            val name = if (it == "1") "newName$it" else "name$it"
            Spreadsheet(it, name, 1)
        }

        spreadsheetDao.updateData(spreadsheets)
        spreadsheetDao.all().subscribe(testSubscriber)

        testSubscriber.assertValue(listOf(
                Spreadsheet("1", "newName1", 1),
                Spreadsheet("2", "name2", 1),
                Spreadsheet("3", "name3", 1),
                Spreadsheet("4", "name4", 1)
        ))
    }

    private fun prepareTestData() {
        val spreadsheetArray = arrayOf(
                Spreadsheet("1", "name1", 1),
                Spreadsheet("2", "name2", 1),
                Spreadsheet("5", "name5", 1),
                Spreadsheet("6", "name6", 1),
                Spreadsheet("7", "name7", 1)
        )

        spreadsheetDao.insertAll(*spreadsheetArray)
    }
}