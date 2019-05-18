package io.rg.mp.ui.transactions

import io.reactivex.schedulers.Schedulers
import io.rg.mp.drive.TransactionService
import io.rg.mp.persistence.dao.TransactionDao
import io.rg.mp.ui.AbstractViewModel
import io.rg.mp.ui.ListTransaction

class TransactionsViewModel(
        private val transactionDao: TransactionDao,
        private val transactionService: TransactionService) : AbstractViewModel() {

    companion object {
        const val REQUEST_AUTHORIZATION_DOWNLOADING_TRANSACTIONS = 3001
    }

    fun loadData(spreadsheetId: String) {
        reloadCategories(spreadsheetId)
        downloadCategories(spreadsheetId)
    }

    private fun reloadCategories(spreadsheetId: String) {
        val disposable = transactionDao.findBySpreadsheetIdSorted(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    subject.onNext(ListTransaction(it))
                }
        compositeDisposable.add(disposable)
    }

    private fun downloadCategories(spreadsheetId: String) {
        val disposable = transactionService.all(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { progressSubject.onNext(1) }
                .doFinally { progressSubject.onNext(-1) }
                .subscribe(
                        {
                            transactionDao.clearTransactions(spreadsheetId)
                            transactionDao.insertAll(*it.list.toTypedArray())
                        },
                        { handleErrors(it, REQUEST_AUTHORIZATION_DOWNLOADING_TRANSACTIONS) }
                )
        compositeDisposable.add(disposable)
    }
}