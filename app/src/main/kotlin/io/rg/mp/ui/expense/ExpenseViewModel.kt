package io.rg.mp.ui.expense

import android.widget.Toast.LENGTH_LONG
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.rg.mp.R
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.persistence.entity.Category
import io.rg.mp.persistence.entity.Spreadsheet
import io.rg.mp.service.drive.SpreadsheetList
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.service.sheet.CategoryService
import io.rg.mp.service.sheet.ExpenseService
import io.rg.mp.service.sheet.data.CategoryList
import io.rg.mp.service.sheet.data.Expense
import io.rg.mp.service.sheet.data.NotSaved
import io.rg.mp.service.sheet.data.Result
import io.rg.mp.service.sheet.data.Saved
import io.rg.mp.ui.auth.AuthViewModel.Companion.REQUEST_AUTHORIZATION
import io.rg.mp.ui.model.ListCategory
import io.rg.mp.ui.model.ListSpreadsheet
import io.rg.mp.ui.model.StartActivity
import io.rg.mp.ui.model.ToastInfo
import io.rg.mp.ui.model.ViewModelResult
import io.rg.mp.utils.Preferences
import java.util.Date

class ExpenseViewModel(
        private val categoryService: CategoryService,
        private val spreadsheetService: SpreadsheetService,
        private val expenseService: ExpenseService,
        private val categoryDao: CategoryDao,
        private val spreadsheetDao: SpreadsheetDao,
        private val preferences: Preferences) {

    private val subject = PublishSubject.create<ViewModelResult>()

    fun viewModelNotifier(): Flowable<ViewModelResult>
            = subject.toFlowable(BackpressureStrategy.BUFFER)

    fun loadData() {
        reloadSpreadsheets()
        reloadCategories()

        downloadData()
    }

    private fun downloadData() {
        Flowable.concat(
                spreadsheetService.list(),
                categoryService.getListBy(preferences.spreadsheetId))
                .subscribeOn(Schedulers.io())
                .subscribe(
                        handleLoading(),
                        handleErrors()
                )
    }

    private fun handleLoading(): (Any) -> Unit {
        return {
            when (it) {
                is SpreadsheetList -> spreadsheetDao.insertAll(*it.list.toTypedArray())
                is CategoryList -> categoryDao.insertAll(*it.list.toTypedArray())
            }

        }
    }

    fun saveExpense(amount: Float, category: Category) {
        val expense = Expense(Date(), amount, "", category)

        expenseService.save(expense, preferences.spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        handleSaving(),
                        handleErrors()
                )
    }

    private fun handleSaving(): (Result) -> Unit {
        return {
            val messageId = when (it) {
                is Saved -> R.string.saved_message
                is NotSaved -> R.string.not_saved_message
            }

            subject.onNext(ToastInfo(messageId, LENGTH_LONG))
        }
    }

    private fun handleErrors(): (Throwable) -> Unit {
        return {
            val result = when (it) {
                is UserRecoverableAuthIOException ->
                    StartActivity(it.intent, REQUEST_AUTHORIZATION)
                else -> ToastInfo(R.string.unknown_error, LENGTH_LONG)
            }
            subject.onNext(result)
        }
    }

    fun onSpreadsheetItemSelected(spreadsheetId: String) {
        preferences.spreadsheetId = spreadsheetId
        reloadCategories()
        downloadCategories()
    }

    fun currentSpreadsheet(spreadsheetList: List<Spreadsheet>): Int =
            spreadsheetList.indexOfFirst { (id) -> id == preferences.spreadsheetId }

    private fun reloadCategories() {
        categoryDao.findBySpreadsheetId(preferences.spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    subject.onNext(ListCategory(it))
                }
    }

    private fun reloadSpreadsheets() {
        spreadsheetDao.all()
                .subscribeOn(Schedulers.io())
                .subscribe {
                    subject.onNext(ListSpreadsheet(it))
                }
    }

    private fun downloadCategories() {
        val spreadsheetId = preferences.spreadsheetId

        categoryService.getListBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { (list) -> categoryDao.insertAll(*list.toTypedArray()) }
    }
}