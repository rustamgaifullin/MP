package io.rg.mp.ui.transactions

import android.content.Intent
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import io.rg.mp.drive.TransactionService
import io.rg.mp.drive.data.TransactionList
import io.rg.mp.persistence.dao.TransactionDao
import io.rg.mp.rule.TrampolineSchedulerRule
import io.rg.mp.ui.ListTransaction
import io.rg.mp.ui.StartActivity
import io.rg.mp.ui.transactions.TransactionsViewModel.Companion.REQUEST_AUTHORIZATION_DOWNLOADING_TRANSACTIONS
import org.junit.Rule
import org.junit.Test

class TransactionsViewModelTest {
    @get:Rule
    val trampolineSchedulerRule = TrampolineSchedulerRule()

    private val transactionDao: TransactionDao = mock()
    private val transactionService: TransactionService = mock()

    private fun viewModel() = TransactionsViewModel(transactionDao, transactionService)

    @Test
    fun `should load transactions`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(transactionDao.findBySpreadsheetIdSorted(spreadsheetId)).thenReturn(
                Flowable.just(emptyList())
        )
        whenever(transactionService.all(spreadsheetId)).thenReturn(
                Flowable.just(TransactionList(emptyList()))
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.loadData(spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValues(ListTransaction(emptyList()))
                .assertNotComplete()
                .dispose()
    }

    @Test
    fun `should show authorization dialog during loading spreadsheets`() {
        val sut = viewModel()
        val spreadsheetId = "id"

        whenever(transactionDao.findBySpreadsheetIdSorted(spreadsheetId)).thenReturn(
                Flowable.empty()
        )
        whenever(transactionService.all(spreadsheetId)).thenReturn(
                Flowable.error(userRecoverableAuthIoException())
        )

        val testSubscriber = sut.viewModelNotifier().test()

        sut.loadData(spreadsheetId)

        testSubscriber
                .assertNoErrors()
                .assertValueCount(1)
                .assertValue {
                    it is StartActivity &&
                            it.requestCode == REQUEST_AUTHORIZATION_DOWNLOADING_TRANSACTIONS
                }
                .assertNotComplete()
                .dispose()
    }

    private fun userRecoverableAuthIoException(): UserRecoverableAuthIOException {
        val wrapper = UserRecoverableAuthException("", Intent())
        return UserRecoverableAuthIOException(wrapper)
    }
}