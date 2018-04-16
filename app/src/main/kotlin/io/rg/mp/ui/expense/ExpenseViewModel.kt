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
import io.rg.mp.persistence.entity.Spreadsheet
import io.rg.mp.ui.AbstractViewModel
import io.rg.mp.ui.BalanceUpdated
import io.rg.mp.ui.DateChanged
import io.rg.mp.ui.ListCategory
import io.rg.mp.ui.SavedSuccessfully
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
        const val REQUEST_AUTHORIZATION_LOADING_CATEGORIES = 2002
    }

    private var lastDate = DateInt.currentDateInt()

    fun currentSpreadsheet(spreadsheetList: List<Spreadsheet>, spreadsheetId: String): Int =
            spreadsheetList.indexOfFirst { (id) -> id == spreadsheetId }

    fun reloadData(spreadsheetId: String) {
        reloadCategories(spreadsheetId)

        downloadCategories(spreadsheetId)
        updateLocale(spreadsheetId)
        reloadBalance(spreadsheetId)
    }

    private fun reloadCategories(spreadsheetId: String) {
        categoryDao.findBySpreadsheetId(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe { subject.onNext(ListCategory(it)) }
    }

    private fun updateLocale(spreadsheetId: String) {
        localeService.getBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { progressSubject.onNext(1) }
                .doFinally { progressSubject.onNext(-1) }
                .subscribe(
                        { spreadsheetDao.updateLocale(it, spreadsheetId) },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING_CATEGORIES) }
                )
    }

    private fun reloadBalance(spreadsheetId: String) {
        balanceService.retrieve(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { progressSubject.onNext(1) }
                .doFinally { progressSubject.onNext(-1) }
                .subscribe { balance ->
                    subject.onNext(BalanceUpdated(balance))
                }
    }

    private fun downloadCategories(spreadsheetId: String) {
        categoryService.getListBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe { progressSubject.onNext(1) }
                .doFinally { progressSubject.onNext(-1) }
                .subscribe(
                        { categoryDao.insertAll(*it.list.toTypedArray()) },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING_CATEGORIES) }
                )
    }

    fun saveExpense(amount: Float, category: Category, description: String, spreadsheetId: String) {
        val spreadsheetId = spreadsheetId
        spreadsheetDao.getLocaleBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe { locale ->
                    val date = formatDate(lastDate, locale)
                    val expense = Expense(date, amount, description, category)

                    transactionService.saveExpense(expense, spreadsheetId)
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    { handleSaving(it) },
                                    { handleErrors(it, REQUEST_AUTHORIZATION_EXPENSE) }
                            )
                }
    }

    private fun handleSaving(result: Result) {
        val messageId = when (result) {
            is Saved -> {
                subject.onNext(SavedSuccessfully())
                R.string.saved_message
            }
            is NotSaved -> R.string.not_saved_message
        }

        subject.onNext(ToastInfo(messageId, LENGTH_LONG))
    }

    fun updateDate(newDate: DateInt, spreadsheetId: String) {
        lastDate = newDate

        spreadsheetDao.getLocaleBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { locale ->
                            val date = formatDate(lastDate, locale)

                            subject.onNext(DateChanged(date))
                        },
                        {
                            //TODO: Not a good way to handle not found rows scenario. Maybe rework with Maybe?
                            subject.onNext(DateChanged(formatDate(lastDate)))
                        }
                )
    }

    fun lastDate() = lastDate
}