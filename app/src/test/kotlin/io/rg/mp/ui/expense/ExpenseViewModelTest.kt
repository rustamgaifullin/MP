package io.rg.mp.ui.expense

import android.content.Intent
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.rg.mp.R
import io.rg.mp.drive.CategoryService
import io.rg.mp.drive.CopyService
import io.rg.mp.drive.FolderService
import io.rg.mp.drive.LocaleService
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.drive.SubscribableTest
import io.rg.mp.drive.TransactionService
import io.rg.mp.drive.data.CategoryList
import io.rg.mp.drive.data.CreationResult
import io.rg.mp.drive.data.NotSaved
import io.rg.mp.drive.data.Saved
import io.rg.mp.drive.data.SpreadsheetList
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.persistence.entity.Category
import io.rg.mp.persistence.entity.Spreadsheet
import io.rg.mp.rule.TrampolineSchedulerRule
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_EXPENSE
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_LOADING_ALL
import io.rg.mp.ui.expense.ExpenseViewModel.Companion.REQUEST_AUTHORIZATION_LOADING_CATEGORIES
import io.rg.mp.ui.expense.model.DateInt
import io.rg.mp.ui.model.CreatedSuccessfully
import io.rg.mp.ui.model.DateChanged
import io.rg.mp.ui.model.ListCategory
import io.rg.mp.ui.model.ListSpreadsheet
import io.rg.mp.ui.model.SavedSuccessfully
import io.rg.mp.ui.model.StartActivity
import io.rg.mp.ui.model.ToastInfo
import io.rg.mp.ui.model.ViewModelResult
import io.rg.mp.utils.Preferences
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ExpenseViewModelTest : SubscribableTest<ViewModelResult>() {

    @get:Rule
    val trampolineSchedulerRule = TrampolineSchedulerRule()

    private val categoryService: CategoryService = mock()
    private val spreadsheetService: SpreadsheetService = mock()
    private val localeService: LocaleService = mock()
    private val transactionService: TransactionService = mock()
    private val copyService: CopyService = mock()
    private val folderService: FolderService = mock()
    private val categoryDao: CategoryDao = mock()
    private val spreadsheetDao: SpreadsheetDao = mock()
    private val preferences: Preferences = mock()

    private fun viewModel() = ExpenseViewModel(
            categoryService,
            spreadsheetService,
            localeService,
            transactionService,
            copyService,
            folderService,
            categoryDao,
            spreadsheetDao,
            preferences
    )

    @Test
    fun `should load categories and spreadsheet`() {
        val sut = viewModel()
        sut.viewModelNotifier().subscribe(testSubscriber)

        whenever(spreadsheetDao.all()).thenReturn(
                Flowable.just(emptyList())
        )
        whenever(categoryDao.findBySpreadsheetId(any())).thenReturn(
                Flowable.just(emptyList())
        )
        whenever(spreadsheetService.list()).thenReturn(
                Flowable.just(SpreadsheetList(emptyList()))
        )
        whenever(categoryService.getListBy(any())).thenReturn(
                Flowable.just(CategoryList(emptyList()))
        )
        whenever(preferences.isSpreadsheetIdAvailable).thenReturn(true)
        whenever(preferences.spreadsheetId).thenReturn("")

        sut.loadData()

        testSubscriber
                .assertNoErrors()
                .assertValues(ListSpreadsheet(emptyList()), ListCategory(emptyList()))
                .assertNotComplete()
    }

    @Test
    fun `should show toast with saved message and notify with successful result when an expense is saved`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(preferences.spreadsheetId).thenReturn(spreadsheetId)
        whenever(spreadsheetDao.getLocaleBy(eq(spreadsheetId))).thenReturn(
                Single.just("en_GB")
        )
        whenever(transactionService.saveExpense(any(), any())).thenReturn(
                Flowable.just(Saved())
        )
        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.saveExpense(123.0F, Category("", ""), "")

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

        whenever(preferences.spreadsheetId).thenReturn(spreadsheetId)
        whenever(spreadsheetDao.getLocaleBy(spreadsheetId)).thenReturn(
                Single.just("en_GB")
        )
        whenever(transactionService.saveExpense(any(), eq(spreadsheetId))).thenReturn(
                Flowable.just(NotSaved())
        )
        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.saveExpense(123.0F, Category("", ""), "")

        testSubscriber
                .assertNoErrors()
                .assertValue { it is ToastInfo && it.messageId == R.string.not_saved_message }
                .assertNotComplete()
    }

    @Test
    fun `should load categories and locale when spreadsheet selected`() {
        val sut = viewModel()
        val category = Category("name", "id")
        val spreadsheetId = "123"

        whenever(preferences.spreadsheetId).thenReturn(spreadsheetId)
        whenever(categoryDao.findBySpreadsheetId(spreadsheetId)).thenReturn(
                Flowable.just(listOf(category))
        )
        whenever(categoryService.getListBy(eq(spreadsheetId))).thenReturn(
                Flowable.just(CategoryList(listOf(category)))
        )
        whenever(localeService.getBy(eq(spreadsheetId))).thenReturn(
                Flowable.just("en_EN")
        )

        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.onSpreadsheetItemSelected(spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue(ListCategory(listOf(category)))
                .assertNotComplete()

        verify(spreadsheetDao).updateLocale(eq("en_EN"), any())
        verify(localeService).getBy(eq(spreadsheetId))
    }

    @Test
    fun `should return current spreadsheet id index`() {
        val spreadsheetList = listOf(
                Spreadsheet("11", "A", 123),
                Spreadsheet("22", "B", 123),
                Spreadsheet("33", "C", 123)
        )
        val sut = viewModel()

        whenever(preferences.spreadsheetId).thenReturn("22")
        val result = sut.currentSpreadsheet(spreadsheetList)

        assertEquals(1, result)
    }

    @Test
    fun `should show authorization dialog during saving when authorization error appeared`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(preferences.spreadsheetId).thenReturn(spreadsheetId)
        whenever(spreadsheetDao.getLocaleBy(spreadsheetId)).thenReturn(
                Single.just("en_GB")
        )
        whenever(transactionService.saveExpense(any(), any())).thenReturn(
                Flowable.error(userRecoverableAuthIoException())
        )
        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.saveExpense(123.0F, Category("", ""), "")

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is StartActivity && it.requestCode == REQUEST_AUTHORIZATION_EXPENSE
                }
                .assertNotComplete()
    }

    @Test
    fun `should show authorization dialog during loading spreadsheets`() {
        val sut = viewModel()

        whenever(spreadsheetDao.all()).thenReturn(
                Flowable.empty()
        )
        whenever(categoryDao.findBySpreadsheetId(any())).thenReturn(
                Flowable.empty()
        )
        whenever(spreadsheetService.list()).thenReturn(
                Flowable.error(userRecoverableAuthIoException())
        )
        whenever(categoryService.getListBy(any())).thenReturn(
                Flowable.error(userRecoverableAuthIoException())
        )
        whenever(preferences.isSpreadsheetIdAvailable).thenReturn(true)
        whenever(preferences.spreadsheetId).thenReturn("")

        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.loadData()

        testSubscriber
                .assertNoErrors()
                .assertValueCount(1)
                .assertValue {
                    it is StartActivity && it.requestCode == REQUEST_AUTHORIZATION_LOADING_ALL
                }
                .assertNotComplete()
    }

    @Test
    fun `should load current category and locale when spreadsheet id is available`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(preferences.isSpreadsheetIdAvailable).thenReturn(true)
        whenever(preferences.spreadsheetId).thenReturn(spreadsheetId)
        whenever(categoryService.getListBy(eq(spreadsheetId))).thenReturn(
                Flowable.just(CategoryList(emptyList()))
        )
        whenever(localeService.getBy(eq(spreadsheetId))).thenReturn(
                Flowable.just("en_EN")
        )
        sut.loadCurrentCategories()

        verify(categoryDao).insertAll(any())
        verify(spreadsheetDao).updateLocale(eq("en_EN"), any())
    }

    @Test
    fun `should not load current category and locale when spreadsheet id is not available`() {
        val sut = viewModel()

        whenever(preferences.isSpreadsheetIdAvailable).thenReturn(false)
        sut.loadCurrentCategories()

        verifyZeroInteractions(categoryService)
        verifyZeroInteractions(categoryDao)
        verifyZeroInteractions(localeService)
        verifyZeroInteractions(spreadsheetDao)
    }

    @Test
    fun `should handle authorization error for loading current category`() {
        val sut = viewModel()

        whenever(preferences.isSpreadsheetIdAvailable).thenReturn(true)
        whenever(preferences.spreadsheetId).thenReturn("")
        whenever(localeService.getBy(any())).thenReturn(
                Flowable.empty()
        )
        whenever(categoryService.getListBy(any())).thenReturn(
                Flowable.error(userRecoverableAuthIoException())
        )
        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.loadCurrentCategories()

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is StartActivity
                            && it.requestCode == REQUEST_AUTHORIZATION_LOADING_CATEGORIES
                }
                .assertNotComplete()
    }

    @Test
    fun `should handle authorization error for loading locale`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(preferences.isSpreadsheetIdAvailable).thenReturn(true)
        whenever(preferences.spreadsheetId).thenReturn(spreadsheetId)
        whenever(categoryService.getListBy(any())).thenReturn(
                Flowable.empty()
        )
        whenever(localeService.getBy(eq(spreadsheetId))).thenReturn(
                Flowable.error(userRecoverableAuthIoException())
        )
        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.loadCurrentCategories()

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is StartActivity
                            && it.requestCode == REQUEST_AUTHORIZATION_LOADING_CATEGORIES
                }
                .assertNotComplete()
    }

    @Test
    fun `should notify to update date button when date was changed`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(preferences.spreadsheetId).thenReturn(spreadsheetId)
        whenever(spreadsheetDao.getLocaleBy(spreadsheetId)).thenReturn(
                Single.just("pl_PL")
        )

        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.updateDate(DateInt(2016, 3, 10))

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is DateChanged && it.date == "10.04.16"
                }
                .assertNotComplete()
    }

    @Test
    fun `should successfully create new spreadsheet and receive event`() {
        val sut = viewModel()
        val newSpreadsheetId = "newId"

        whenever(copyService.copy()).thenReturn(
                Single.just(CreationResult(newSpreadsheetId))
        )
        whenever(folderService.folderIdForCurrentYear()).thenReturn(
                Single.just("folderId")
        )
        whenever(folderService.moveToFolder(eq(newSpreadsheetId), any())).thenReturn(
                Completable.complete()
        )
        whenever(transactionService.clearAllTransactions(newSpreadsheetId)).thenReturn(
                Completable.complete()
        )

        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.createNewSpreadsheet()

        verify(copyService).copy()
        verify(folderService).moveToFolder(eq(newSpreadsheetId), any())
        verify(transactionService).clearAllTransactions(newSpreadsheetId)
        verify(spreadsheetService, never()).deleteSpreadsheet(newSpreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is CreatedSuccessfully && it.spreadsheetId == "newId"
                }
                .assertNotComplete()
    }

    @Test
    fun `should delete created earlier spreadsheet if error will occur during moving to folder`() {
        val sut = viewModel()
        val newSpreadsheetId = "newId"

        whenever(copyService.copy()).thenReturn(
                Single.just(CreationResult(newSpreadsheetId))
        )
        whenever(folderService.folderIdForCurrentYear()).thenReturn(
                Single.just("folderId")
        )
        whenever(folderService.moveToFolder(eq(newSpreadsheetId), any())).thenReturn(
                Completable.error(Exception())
        )
        whenever(spreadsheetService.deleteSpreadsheet(newSpreadsheetId)).thenReturn(
                Completable.complete()
        )

        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.createNewSpreadsheet()

        verify(spreadsheetService).deleteSpreadsheet(newSpreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is ToastInfo
                }
                .assertNotComplete()
    }

    @Test
    fun `should delete created earlier spreadsheet if error will occur during clearing old transactions`() {
        val sut = viewModel()
        val newSpreadsheetId = "id"

        whenever(copyService.copy()).thenReturn(
                Single.just(CreationResult(newSpreadsheetId))
        )
        whenever(folderService.folderIdForCurrentYear()).thenReturn(
                Single.just("folderId")
        )
        whenever(folderService.moveToFolder(eq(newSpreadsheetId), any())).thenReturn(
                Completable.complete()
        )
        whenever(transactionService.clearAllTransactions(newSpreadsheetId)).thenReturn(
                Completable.error(Exception())
        )
        whenever(spreadsheetService.deleteSpreadsheet(newSpreadsheetId)).thenReturn(
                Completable.complete()
        )

        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.createNewSpreadsheet()

        verify(spreadsheetService).deleteSpreadsheet(newSpreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is ToastInfo
                }
                .assertNotComplete()
    }

    @Test
    fun `should delete created earlier spreadsheet if error will occur during getting folder id`() {
        val sut = viewModel()
        val newSpreadsheetId = "id"

        whenever(copyService.copy()).thenReturn(
                Single.just(CreationResult(newSpreadsheetId))
        )
        whenever(folderService.folderIdForCurrentYear()).thenReturn(
                Single.error(Exception())
        )

        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.createNewSpreadsheet()

        verify(spreadsheetService).deleteSpreadsheet(newSpreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is ToastInfo
                }
                .assertNotComplete()
    }


    private fun userRecoverableAuthIoException(): UserRecoverableAuthIOException {
        val wrapper = UserRecoverableAuthException("", Intent())
        return UserRecoverableAuthIOException(wrapper)
    }
}