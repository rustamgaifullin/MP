package io.rg.mp.ui.expense

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import io.reactivex.subscribers.TestSubscriber
import io.rg.mp.R
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.persistence.entity.Category
import io.rg.mp.persistence.entity.Spreadsheet
import io.rg.mp.rule.TrampolineSchedulerRule
import io.rg.mp.service.drive.SpreadsheetList
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.service.sheet.CategoryService
import io.rg.mp.service.sheet.ExpenseService
import io.rg.mp.service.sheet.data.CategoryList
import io.rg.mp.service.sheet.data.NotSaved
import io.rg.mp.service.sheet.data.Saved
import io.rg.mp.ui.model.ToastInfo
import io.rg.mp.utils.Preferences
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ExpenseViewModelTest {

    @get:Rule
    val trampolineSchedulerRule = TrampolineSchedulerRule()

    private val categoryService: CategoryService = mock()
    private val spreadsheetService: SpreadsheetService = mock()
    private val expenseService: ExpenseService = mock()
    private val categoryDao: CategoryDao = mock()
    private val spreadsheetDao: SpreadsheetDao = mock()
    private val preferences: Preferences = mock()

    private lateinit var categoryTestSubscriber: TestSubscriber<List<Category>>
    private lateinit var spreadsheetTestSubscriber: TestSubscriber<List<Spreadsheet>>


    private fun viewModel() = ExpenseViewModel(
            categoryService,
            spreadsheetService,
            expenseService,
            categoryDao,
            spreadsheetDao,
            preferences
    )

    @Before
    fun setup() {
        categoryTestSubscriber = TestSubscriber()
        spreadsheetTestSubscriber = TestSubscriber()
    }

    @Test
    fun `should load categories and spreadsheet`() {
        val sut = viewModel()
        sut.getCategories().subscribe(categoryTestSubscriber)
        sut.getSpreadsheets().subscribe(spreadsheetTestSubscriber)

        whenever(spreadsheetDao.all()).thenReturn(Flowable.just(emptyList()))
        whenever(categoryDao.findBySpreadsheetId(any())).thenReturn(Flowable.just(emptyList()))
        whenever(spreadsheetService.list()).thenReturn(Flowable.just(SpreadsheetList(emptyList())))
        whenever(categoryService.getListBy(any())).thenReturn(Flowable.just(CategoryList(emptyList())))
        whenever(preferences.isSpreadsheetIdAvailable).thenReturn(true)
        whenever(preferences.spreadsheetId).thenReturn("")

        sut.loadData()

        categoryTestSubscriber.assertNoErrors()
        categoryTestSubscriber.assertValue { it.isEmpty() }

        spreadsheetTestSubscriber.assertNoErrors()
        spreadsheetTestSubscriber.assertValue { it.isEmpty() }
    }

    @Test
    fun `should show toast with saved message when an expense is saved`() {
        val sut = viewModel()
        val testSubscriber = TestSubscriber<ToastInfo>()

        whenever(preferences.spreadsheetId).thenReturn("")
        whenever(expenseService.save(any(), any())).thenReturn(Flowable.just(Saved()))
        sut.saveExpense(123.0F, Category("", ""))
                .subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { it.messageId == R.string.saved_message }
        testSubscriber.assertComplete()
    }

    @Test
    fun `should show toast with not saved message when an expense is not saved`() {
        val sut = viewModel()
        val testSubscriber = TestSubscriber<ToastInfo>()

        whenever(preferences.spreadsheetId).thenReturn("")
        whenever(expenseService.save(any(), any())).thenReturn(Flowable.just(NotSaved()))
        sut.saveExpense(123.0F, Category("", ""))
                .subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { it.messageId == R.string.not_saved_message }
        testSubscriber.assertComplete()
    }

    @Test
    fun `should load categories when spreadsheet selected`() {
        val sut = viewModel()
        val category = Category("name", "id")
        val spreadsheetId = "123"

        whenever(preferences.spreadsheetId).thenReturn(spreadsheetId)
        whenever(categoryDao.findBySpreadsheetId(spreadsheetId))
                .thenReturn(Flowable.just(listOf(category)))
        whenever(categoryService.getListBy(any()))
                .thenReturn(Flowable.just(CategoryList(listOf(category))))

        sut.getCategories().subscribe(categoryTestSubscriber)
        sut.onSpreadsheetItemSelected(spreadsheetId)

        categoryTestSubscriber.assertNoErrors()
        categoryTestSubscriber.assertValue {
            it.isNotEmpty() && it.first().name == "name" && it.first().spreadsheetId == "id"
        }
        categoryTestSubscriber.assertNotComplete()
    }

    @Test
    fun `should return current spreadsheet id index`() {
        val spreadsheetList = listOf(
                Spreadsheet("11", "A"),
                Spreadsheet("22", "B"),
                Spreadsheet("33", "C")
        )
        val sut = viewModel()

        whenever(preferences.spreadsheetId).thenReturn("22")
        val result = sut.currentSpreadsheet(spreadsheetList)

        assertEquals(1, result)
    }
}