package io.rg.mp.ui.spreadsheet

import android.content.Intent
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.rg.mp.drive.FolderService
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.drive.TransactionService
import io.rg.mp.drive.data.CreationResult
import io.rg.mp.drive.data.SpreadsheetList
import io.rg.mp.persistence.dao.FailedSpreadsheetDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.persistence.entity.FailedSpreadsheet
import io.rg.mp.rule.TrampolineSchedulerRule
import io.rg.mp.ui.CreatedSuccessfully
import io.rg.mp.ui.ListSpreadsheet
import io.rg.mp.ui.RenamedSuccessfully
import io.rg.mp.ui.StartActivity
import io.rg.mp.ui.ToastInfo
import io.rg.mp.ui.spreadsheet.SpreadsheetViewModel.Companion.REQUEST_AUTHORIZATION_FOR_DELETE
import io.rg.mp.ui.spreadsheet.SpreadsheetViewModel.Companion.REQUEST_AUTHORIZATION_LOADING_SPREADSHEETS
import io.rg.mp.ui.spreadsheet.SpreadsheetViewModel.Companion.REQUEST_AUTHORIZATION_NEW_SPREADSHEET
import io.rg.mp.ui.spreadsheet.SpreadsheetViewModel.Companion.REQUEST_DO_NOTHING
import io.rg.mp.utils.getLocaleInstance
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.assertEquals

class SpreadsheetViewModelTest {
    @get:Rule
    val trampolineSchedulerRule = TrampolineSchedulerRule()

    private val spreadsheetService: SpreadsheetService = mock()
    private val folderService: FolderService = mock()
    private val spreadsheetDao: SpreadsheetDao = mock()
    private val transactionService: TransactionService = mock()
    private val failedSpreadsheetDao: FailedSpreadsheetDao = mock()

    private fun viewModel() = SpreadsheetViewModel(
            spreadsheetDao,
            folderService,
            transactionService,
            spreadsheetService,
            failedSpreadsheetDao
    )

    @Test
    fun `should load spreadsheet`() {
        val sut = viewModel()

        whenever(spreadsheetDao.allSorted()).thenReturn(
                Flowable.just(emptyList())
        )
        whenever(spreadsheetService.list()).thenReturn(
                Flowable.just(SpreadsheetList(emptyList()))
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.reloadData()

        testSubscriber
                .assertNoErrors()
                .assertValues(ListSpreadsheet(emptyList()))
                .assertNotComplete()
    }

    @Test
    fun `should show authorization dialog during loading spreadsheets`() {
        val sut = viewModel()

        whenever(spreadsheetDao.allSorted()).thenReturn(
                Flowable.empty()
        )
        whenever(spreadsheetService.list()).thenReturn(
                Flowable.error(userRecoverableAuthIoException())
        )

        val testSubscriber = sut.viewModelNotifier().test()

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

        whenever(folderService.copy("")).thenReturn(
                Single.just(CreationResult(newSpreadsheetId))
        )
        whenever(transactionService.clearAllTransactions(newSpreadsheetId)).thenReturn(
                Completable.complete()
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.createNewSpreadsheet("", "")

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is CreatedSuccessfully && it.spreadsheetId == "newId"
                }
                .assertNotComplete()

        verify(folderService).copy(anyString(), anyString())
        verify(transactionService).clearAllTransactions(newSpreadsheetId)
        verify(failedSpreadsheetDao).insert(FailedSpreadsheet(spreadsheetId = newSpreadsheetId))
        verify(failedSpreadsheetDao).delete(newSpreadsheetId)
    }

    @Test
    fun `should show toast info if error occurs during moving to folder`() {
        val sut = viewModel()
        val newSpreadsheetId = "newId"

        whenever(folderService.copy("")).thenReturn(
                Single.just(CreationResult(newSpreadsheetId))
        )
        whenever(spreadsheetService.deleteSpreadsheet(newSpreadsheetId)).thenReturn(
                Completable.complete()
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.createNewSpreadsheet("", "")

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is ToastInfo
                }
                .assertNotComplete()
    }

    @Test
    fun `should show authorization dialog if error occurs during clearing old transactions`() {
        val sut = viewModel()
        val newSpreadsheetId = "id"

        whenever(folderService.copy("")).thenReturn(
                Single.just(CreationResult(newSpreadsheetId))
        )
        whenever(transactionService.clearAllTransactions(newSpreadsheetId)).thenReturn(
                Completable.error(userRecoverableAuthIoException())
        )
        whenever(spreadsheetService.deleteSpreadsheet(newSpreadsheetId)).thenReturn(
                Completable.complete()
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.createNewSpreadsheet("", "")

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is StartActivity &&
                            it.requestCode == REQUEST_AUTHORIZATION_NEW_SPREADSHEET
                }
                .assertNotComplete()
    }

    @Test
    fun `should show toast info if error occurs during clearing old transactions`() {
        val sut = viewModel()
        val newSpreadsheetId = "id"

        whenever(folderService.copy("")).thenReturn(
                Single.just(CreationResult(newSpreadsheetId))
        )
        whenever(transactionService.clearAllTransactions(newSpreadsheetId)).thenReturn(
                Completable.error(Exception())
        )
        whenever(spreadsheetService.deleteSpreadsheet(newSpreadsheetId)).thenReturn(
                Completable.complete()
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.createNewSpreadsheet("", "")

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is ToastInfo
                }
                .assertNotComplete()
    }

    @Test
    fun `should show toast info if error occurs during getting folder id`() {
        val sut = viewModel()
        val newSpreadsheetId = "id"

        whenever(folderService.copy("")).thenReturn(
                Single.just(CreationResult(newSpreadsheetId))
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.createNewSpreadsheet("", "")

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is ToastInfo
                }
                .assertNotComplete()
    }

    @Test
    fun `should return name in format current month current year`() {
        val sut = viewModel()
        val simpleDateFormat = SimpleDateFormat("LLLL YYYY", getLocaleInstance())
        assertEquals(simpleDateFormat.format(Date()), sut.createSpreadsheetName())
    }

    @Test
    fun `should delete failed spreadsheets`() {
        val sut = viewModel()

        whenever(failedSpreadsheetDao.all()).thenReturn(
                Single.just(listOf(
                        FailedSpreadsheet(0, "0"),
                        FailedSpreadsheet(1, "1")
                ))
        )
        whenever(spreadsheetService.deleteSpreadsheet(anyString())).thenReturn(
                Completable.complete()
        )

        sut.deleteFailedSpreadsheets()

        verify(failedSpreadsheetDao, times(2)).delete(anyString())
        verify(spreadsheetDao, times(2)).delete(anyString())
    }

    @Test
    fun `should show authorization dialog during deleting spreadsheet`() {
        val sut = viewModel()

        whenever(failedSpreadsheetDao.all()).thenReturn(
                Single.just(listOf(
                        FailedSpreadsheet(0, "0")
                ))
        )
        whenever(spreadsheetService.deleteSpreadsheet(anyString())).thenReturn(
                Completable.error(userRecoverableAuthIoException())
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.deleteFailedSpreadsheets()

        testSubscriber.assertNoErrors()
                .assertValue {
                    it is StartActivity &&
                            it.requestCode == REQUEST_AUTHORIZATION_FOR_DELETE
                }
                .assertNotComplete()

        verify(failedSpreadsheetDao, never()).delete(anyString())
        verify(spreadsheetDao, never()).delete(anyString())
    }

    @Test
    fun `should show authorization dialog during renaming`() {
        val sut = viewModel()
        val spreadsheetId = "id"
        val newName = "newName"

        whenever(folderService.rename(spreadsheetId, newName)).thenReturn(
                Completable.complete()
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.renameSpreadsheet(spreadsheetId, newName)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is RenamedSuccessfully
                }
                .assertNotComplete()
    }

    @Test
    fun `should successfully rename a spreadsheet and receive event`() {
        val sut = viewModel()
        val spreadsheetId = "id"
        val newName = "newName"

        whenever(folderService.rename(spreadsheetId, newName)).thenReturn(
                Completable.error(userRecoverableAuthIoException())
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.renameSpreadsheet(spreadsheetId, newName)

        testSubscriber
                .assertNoErrors()
                .assertValue {
                    it is StartActivity &&
                            it.requestCode == REQUEST_DO_NOTHING
                }
                .assertNotComplete()
    }

    private fun userRecoverableAuthIoException(): UserRecoverableAuthIOException {
        val wrapper = UserRecoverableAuthException("", Intent())
        return UserRecoverableAuthIOException(wrapper)
    }
}