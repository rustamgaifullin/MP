package io.rg.mp.ui.expense

import android.widget.Toast.LENGTH_LONG
import io.reactivex.schedulers.Schedulers
import io.rg.mp.R
import io.rg.mp.drive.BalanceService
import io.rg.mp.drive.CategoryService
import io.rg.mp.drive.LocaleService
import io.rg.mp.drive.TransactionService
import io.rg.mp.drive.data.Expense
import io.rg.mp.drive.data.NotSaved
import io.rg.mp.drive.data.Result
import io.rg.mp.drive.data.Saved
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.persistence.entity.Category
import io.rg.mp.ui.AbstractViewModel
import io.rg.mp.ui.ListCategory
import io.rg.mp.ui.SavedSuccessfully
import io.rg.mp.ui.SpreadsheetData
import io.rg.mp.ui.ToastInfo
import io.rg.mp.ui.expense.model.DateInt
import io.rg.mp.utils.formatDate

class ExpenseViewModel(
        private val categoryService: CategoryService,
        private val localeService: LocaleService,
        private val transactionService: TransactionService,
        private val balanceService: BalanceService,
        private val categoryDao: CategoryDao,
        private val spreadsheetDao: SpreadsheetDao) : AbstractViewModel() {

    companion object {
        const val REQUEST_AUTHORIZATION_EXPENSE = 2000
        const val REQUEST_AUTHORIZATION_LOADING = 2002
    }

    fun reloadData(spreadsheetId: String) {
        reloadSpreadsheet(spreadsheetId)
        reloadCategories(spreadsheetId)

        downloadCategories(spreadsheetId)
        updateLocale(spreadsheetId)
        updateBalance(spreadsheetId)
    }

    private fun reloadSpreadsheet(spreadsheetId: String) {
        val disposable = spreadsheetDao.getSpreadsheetBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe { subject.onNext(SpreadsheetData(it)) }
        compositeDisposable.add(disposable)
    }

    private fun reloadCategories(spreadsheetId: String) {
        val disposable = categoryDao.findBySpreadsheetId(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe { subject.onNext(ListCategory(it)) }
        compositeDisposable.add(disposable)
    }

    private fun updateLocale(spreadsheetId: String) {
        val disposable = localeService.getBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { progressSubject.onNext(1) }
                .doFinally { progressSubject.onNext(-1) }
                .subscribe(
                        { spreadsheetDao.updateLocale(it, spreadsheetId) },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING) }
                )
        compositeDisposable.add(disposable)
    }

    private fun updateBalance(spreadsheetId: String) {
        val disposable = balanceService.retrieve(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { progressSubject.onNext(1) }
                .doFinally { progressSubject.onNext(-1) }
                .subscribe(
                        { spreadsheetDao.updateFromBalance(it, spreadsheetId) },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING) }
                )
        compositeDisposable.add(disposable)
    }

    private fun downloadCategories(spreadsheetId: String) {
        val disposable = categoryService.getListBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe { progressSubject.onNext(1) }
                .doFinally { progressSubject.onNext(-1) }
                .subscribe(
                        { categoryDao.insertAll(*it.list.toTypedArray()) },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING) }
                )
        compositeDisposable.add(disposable)
    }

    fun saveExpense(
            amount: Float,
            category: Category,
            description: String,
            spreadsheetId: String,
            date: DateInt) {
        val disposable = spreadsheetDao.getLocaleBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe { locale ->
                    val formattedDate = formatDate(date, locale)
                    val expense = Expense(formattedDate, amount, description, category)

                    transactionService.saveExpense(expense, spreadsheetId)
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    { handleSaving(it) },
                                    { handleErrors(it, REQUEST_AUTHORIZATION_EXPENSE) }
                            )
                }
        compositeDisposable.add(disposable)
    }

    private fun handleSaving(result: Result) {
        val messageId = when (result) {
            is Saved -> {
                subject.onNext(SavedSuccessfully)
                updateBalance(result.spreadsheetId)
                R.string.saved_message
            }
            is NotSaved -> R.string.not_saved_message
        }

        subject.onNext(ToastInfo(messageId, LENGTH_LONG))
    }
}