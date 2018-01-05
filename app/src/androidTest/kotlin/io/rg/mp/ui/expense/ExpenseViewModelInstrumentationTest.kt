package io.rg.mp.ui.expense

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import io.reactivex.subscribers.TestSubscriber
import io.rg.mp.drive.CategoryService
import io.rg.mp.drive.ExpenseService
import io.rg.mp.drive.LocaleService
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.drive.data.SpreadsheetList
import io.rg.mp.persistence.Database
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.persistence.entity.Spreadsheet
import io.rg.mp.utils.Preferences
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@SmallTest
@RunWith(AndroidJUnit4::class)
class ExpenseViewModelInstrumentationTest {

    private lateinit var spreadsheetDao: SpreadsheetDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var db: Database
    private lateinit var testSubscriber: TestSubscriber<List<Spreadsheet>>

    private val categoryService: CategoryService = mock()
    private val spreadsheetService: SpreadsheetService = mock()
    private val localeService: LocaleService = mock()
    private val expenseService: ExpenseService = mock()
    private val preferences: Preferences = mock()

    private fun viewModel() = ExpenseViewModel(
            categoryService,
            spreadsheetService,
            localeService,
            expenseService,
            categoryDao,
            spreadsheetDao,
            preferences
    )

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Room.inMemoryDatabaseBuilder(context, Database::class.java).build()
        spreadsheetDao = db.spreadsheetDao()
        categoryDao = db.categoryDao()

        testSubscriber = TestSubscriber()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun shouldUpdateRequiredData() {
        val sut = viewModel()
        prepareTestData()

        val listOfIds = listOf("1", "2", "3", "4")
        val listOfSpreadsheets = listOfIds.map {
            val name = if (it == "1") "newName$it" else "name$it"
            Spreadsheet(it, name, 1)
        }


        whenever(spreadsheetService.list()).thenReturn(
                Flowable.just(SpreadsheetList(listOfSpreadsheets))
        )

        sut.loadData()

        spreadsheetDao.all().subscribe(testSubscriber)
        testSubscriber.assertValue(listOf(
                Spreadsheet("1", "newName1", 1),
                Spreadsheet("2", "name2", 1),
                Spreadsheet("3", "newName3", 1),
                Spreadsheet("4", "newName4", 1)
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