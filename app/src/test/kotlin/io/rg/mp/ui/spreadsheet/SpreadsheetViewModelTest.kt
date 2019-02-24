package io.rg.mp.ui.spreadsheet

import android.content.Intent
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.rg.mp.drive.CopyService
import io.rg.mp.drive.FolderService
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.drive.SubscribableTest
import io.rg.mp.drive.TransactionService
import io.rg.mp.drive.data.CreationResult
import io.rg.mp.drive.data.SpreadsheetList
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.rule.TrampolineSchedulerRule
import io.rg.mp.ui.CreatedSuccessfully
import io.rg.mp.ui.ListSpreadsheet
import io.rg.mp.ui.StartActivity
import io.rg.mp.ui.ToastInfo
import io.rg.mp.ui.ViewModelResult
import io.rg.mp.ui.spreadsheet.SpreadsheetViewModel.Companion.REQUEST_AUTHORIZATION_LOADING_SPREADSHEETS
import org.junit.Rule
import org.junit.Test

class SpreadsheetViewModelTest : SubscribableTest<ViewModelResult>() {

    @get:Rule
    val trampolineSchedulerRule = TrampolineSchedulerRule()

    private val spreadsheetService: SpreadsheetService = mock()
    private val copyService: CopyService = mock()
    private val folderService: FolderService = mock()
    private val spreadsheetDao: SpreadsheetDao = mock()
    private val transactionService: TransactionService = mock()

    private fun viewModel() = SpreadsheetViewModel(
            spreadsheetDao,
            copyService,
            folderService,
            transactionService,
            spreadsheetService
    )

    @Test
    fun `should load spreadsheet`() {
        val sut = viewModel()
        sut.viewModelNotifier().subscribe(testSubscriber)

        whenever(spreadsheetDao.all()).thenReturn(
                Flowable.just(emptyList())
        )
        whenever(spreadsheetService.list()).thenReturn(
                Flowable.just(SpreadsheetList(emptyList()))
        )

        sut.reloadData()

        testSubscriber
                .assertNoErrors()
                .assertValues(ListSpreadsheet(emptyList()))
                .assertNotComplete()
    }


    @Test
    fun `should show authorization dialog during loading spreadsheets`() {
        val sut = viewModel()

        whenever(spreadsheetDao.all()).thenReturn(
                Flowable.empty()
        )
        whenever(spreadsheetService.list()).thenReturn(
                Flowable.error(userRecoverableAuthIoException())
        )

        sut.viewModelNotifier().subscribe(testSubscriber)
        sut.reloadData()

        testSubscriber
                .assertNoErrors()
                .assertValueCount(1)
                .assertValue {
                    it is StartActivity &&
                            it.requestCode == REQUEST_AUTHORIZATION_LOADING_SPREADSHEETS
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