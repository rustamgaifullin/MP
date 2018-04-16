package io.rg.mp.ui.expense

import android.arch.persistence.room.EmptyResultSetException
import android.content.Intent
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.subscribers.TestSubscriber
import io.rg.mp.R
import io.rg.mp.drive.BalanceService
import io.rg.mp.drive.CategoryService
import io.rg.mp.drive.LocaleService
import io.rg.mp.drive.SubscribableTest
import io.rg.mp.drive.TransactionService
import io.rg.mp.drive.data.Balance
import io.rg.mp.drive.data.CategoryList
import io.rg.mp.drive.data.NotSaved
import io.rg.mp.drive.data.Saved
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.persistence.entity.Category
import io.rg.mp.persistence.entity.Spreadsheet
import io.rg.mp.rule.TrampolineSchedulerRule
import io.rg.mp.ui.BalanceUpdated
import io.rg.mp.ui.DateChanged
import io.rg.mp.ui.ListCategory
import io.rg.mp.ui.SavedSuccessfully
import io.rg.mp.ui.StartActivity
import io.rg.mp.ui.ToastInfo
import io.rg.mp.ui.ViewModelResult
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_EXPENSE
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_LOADING_CATEGORIES
import io.rg.mp.ui.expense.model.DateInt
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ExpenseViewModelTest : SubscribableTest<ViewModelResult>() {

    @get:Rule
    val trampolineSchedulerRule = TrampolineSchedulerRule()

    private val categoryService: CategoryService = mock()
    private val localeService: LocaleService = mock()
    private val transactionService: TransactionService = mock()
    private val balanceService: BalanceService = mock()
    private val categoryDao: CategoryDao = mock()
    private val spreadsheetDao: SpreadsheetDao = mock()

    private fun viewModel() = ExpenseViewModel(
            categoryService,
            localeService,
            transactionService,
            balanceService,
            categoryDao,
            spreadsheetDao
    )

    @Test
    fun `should reload all available data for spreadsheet`() {
        val sut = viewModel()
        val category = Category("name", "id")
        val spreadsheetId = "123"

        whenever(categoryDao.findBySpreadsheetId(spreadsheetId)).thenReturn(
                Flowable.just(listOf(category))
        )
        whenever(categoryService.getListBy(eq(spreadsheetId))).thenReturn(
                Flowable.just(CategoryList(listOf(category)))
        )
        whenever(localeService.getBy(eq(spreadsheetId))).thenReturn(
                Flowable.just("en_EN")
        )
        whenever(balanceService.retrieve(spreadsheetId)).thenReturn(
                Single.just(Balance())
        )

        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.reloadData(spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValues(ListCategory(listOf(category)), BalanceUpdated(Balance()))
                .assertNotComplete()

        verify(spreadsheetDao).updateLocale(eq("en_EN"), any())
        verify(localeService).getBy(eq(spreadsheetId))
    }

    @Test
    fun `should handle authorization error for loading locale`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(categoryDao.findBySpreadsheetId(spreadsheetId)).thenReturn(
                Flowable.empty()
        )
        whenever(categoryService.getListBy(spreadsheetId)).thenReturn(
                Flowable.empty()
        )
        whenever(localeService.getBy(eq(spreadsheetId))).thenReturn(
                Flowable.error(userRecoverableAuthIoException())
        )
        whenever(balanceService.retrieve(spreadsheetId)).thenReturn(
                Single.never()
        )
        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.reloadData(spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is StartActivity
                            && it.requestCode == REQUEST_AUTHORIZATION_LOADING_CATEGORIES
                }
                .assertNotComplete()
    }

    @Test
    fun `should handle authorization error for loading current category`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(categoryDao.findBySpreadsheetId(spreadsheetId)).thenReturn(
                Flowable.empty()
        )
        whenever(localeService.getBy(spreadsheetId)).thenReturn(
                Flowable.empty()
        )
        whenever(categoryService.getListBy(spreadsheetId)).thenReturn(
                Flowable.error(userRecoverableAuthIoException())
        )
        whenever(balanceService.retrieve(spreadsheetId)).thenReturn(
                Single.never()
        )
        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.reloadData(spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is StartActivity
                            && it.requestCode == REQUEST_AUTHORIZATION_LOADING_CATEGORIES
                }
                .assertNotComplete()
    }

    @Test
    fun `should show toast with saved message and notify with successful result when an expense is saved`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(spreadsheetDao.getLocaleBy(eq(spreadsheetId))).thenReturn(
                Single.just("en_GB")
        )
        whenever(transactionService.saveExpense(any(), any())).thenReturn(
                Flowable.just(Saved())
        )
        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.saveExpense(
                123.0F, Category("", ""), "", spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValueAt(0, { it is SavedSuccessfully })
                .assertValueAt(1, { it is ToastInfo && it.messageId == R.string.saved_message })
                .assertNotComplete()
    }

    @Test
    fun `should show toast with not saved message when an expense is not saved`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(spreadsheetDao.getLocaleBy(spreadsheetId)).thenReturn(
                Single.just("en_GB")
        )
        whenever(transactionService.saveExpense(any(), eq(spreadsheetId))).thenReturn(
                Flowable.just(NotSaved())
        )
        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.saveExpense(
                123.0F, Category("", ""), "", spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue { it is ToastInfo && it.messageId == R.string.not_saved_message }
                .assertNotComplete()
    }

    @Test
    fun `should return current spreadsheet id index`() {
        val spreadsheetList = listOf(
                Spreadsheet("11", "A", 123),
                Spreadsheet("22", "B", 123),
                Spreadsheet("33", "C", 123)
        )
        val sut = viewModel()

        val result = sut.currentSpreadsheet(spreadsheetList, "22")

        assertEquals(1, result)
    }

    @Test
    fun `should show authorization dialog during saving when authorization error appeared`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(spreadsheetDao.getLocaleBy(spreadsheetId)).thenReturn(
                Single.just("en_GB")
        )
        whenever(transactionService.saveExpense(any(), any())).thenReturn(
                Flowable.error(userRecoverableAuthIoException())
        )
        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.saveExpense(
                123.0F, Category("", ""), "", spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is StartActivity && it.requestCode == REQUEST_AUTHORIZATION_EXPENSE
                }
                .assertNotComplete()
    }

    @Test
    fun `should notify to update date button when date was changed`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(spreadsheetDao.getLocaleBy(spreadsheetId)).thenReturn(
                Single.just("pl_PL")
        )

        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.updateDate(DateInt(2016, 3, 10), spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is DateChanged && it.date == "10.04.16"
                }
                .assertNotComplete()
    }

    @Test
    fun `should return formatted date even if there are no records in db`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(spreadsheetDao.getLocaleBy(spreadsheetId)).thenReturn(
                Single.error(EmptyResultSetException(""))
        )

        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.updateDate(DateInt(2018, 3, 4), spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is DateChanged && it.date == "4/4/18"
                }
    }

    @Test
    fun `should receive progress notifications`() {
        val sut = viewModel()
        val progressTestSubscriber = TestSubscriber<Boolean>()
        val spreadsheetId = "id"

        whenever(categoryDao.findBySpreadsheetId(spreadsheetId)).thenReturn(
                Flowable.empty()
        )
        whenever(categoryService.getListBy(eq(spreadsheetId))).thenReturn(
                Flowable.empty()
        )
        whenever(localeService.getBy(eq(spreadsheetId))).thenReturn(
                Flowable.empty()
        )
        whenever(balanceService.retrieve(spreadsheetId)).thenReturn(
                Single.just(Balance())
        )

        sut.isOperationInProgress().toFlowable(BackpressureStrategy.BUFFER)
                .subscribe(progressTestSubscriber)
        sut.reloadData(spreadsheetId)

        progressTestSubscriber
                .assertNoErrors()
                .assertValues(false, true, false, true, false, true, false)
    }

    private fun userRecoverableAuthIoException(): UserRecoverableAuthIOException {
        val wrapper = UserRecoverableAuthException("", Intent())
        return UserRecoverableAuthIOException(wrapper)
    }
}