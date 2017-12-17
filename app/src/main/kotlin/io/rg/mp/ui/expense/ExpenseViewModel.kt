package io.rg.mp.ui.expense

import android.util.Log
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
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.service.sheet.CategoryService
import io.rg.mp.service.sheet.ExpenseService
import io.rg.mp.service.sheet.LocaleService
import io.rg.mp.service.sheet.data.Expense
import io.rg.mp.service.sheet.data.NotSaved
import io.rg.mp.service.sheet.data.Result
import io.rg.mp.service.sheet.data.Saved
import io.rg.mp.ui.model.ListCategory
import io.rg.mp.ui.model.ListSpreadsheet
import io.rg.mp.ui.model.SavedSuccessfully
import io.rg.mp.ui.model.StartActivity
import io.rg.mp.ui.model.ToastInfo
import io.rg.mp.ui.model.ViewModelResult
import io.rg.mp.utils.Preferences
import io.rg.mp.utils.currentDate

class ExpenseViewModel(
        private val categoryService: CategoryService,
        private val spreadsheetService: SpreadsheetService,
        private val localeService: LocaleService,
        private val expenseService: ExpenseService,
        private val categoryDao: CategoryDao,
        private val spreadsheetDao: SpreadsheetDao,
        private val preferences: Preferences) {

    companion object {
        const val REQUEST_AUTHORIZATION_EXPENSE = 2000
        const val REQUEST_AUTHORIZATION_LOADING_ALL = 2001
        const val REQUEST_AUTHORIZATION_LOADING_CATEGORIES = 2002
    }

    private val subject = PublishSubject.create<ViewModelResult>()

    fun viewModelNotifier(): Flowable<ViewModelResult>
            = subject.toFlowable(BackpressureStrategy.BUFFER)

    fun currentSpreadsheet(spreadsheetList: List<Spreadsheet>): Int =
            spreadsheetList.indexOfFirst { (id) -> id == preferences.spreadsheetId }

    fun onSpreadsheetItemSelected(spreadsheetId: String) {
        preferences.spreadsheetId = spreadsheetId
        reloadCategories()
        downloadCategories()
        updateLocale(spreadsheetId)
    }

    fun loadCurrentCategories() {
        if (preferences.isSpreadsheetIdAvailable) {
            downloadCategories()
            updateLocale(preferences.spreadsheetId)
        }
    }

    private fun updateLocale(spreadsheetId: String) {
        localeService.getBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            Log.d("ExpenseViewModel", "update locale: $it for spreadsheet: $spreadsheetId")
                            val result = spreadsheetDao.updateLocale(it, spreadsheetId)
                            Log.d("ExpenseViewModel", "result code: $result")
                        },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING_CATEGORIES) }
                )
    }

    fun loadData() {
        reloadSpreadsheets()
        reloadCategories()

        downloadData()
    }

    private fun reloadSpreadsheets() {
        spreadsheetDao.all()
                .subscribeOn(Schedulers.io())
                .subscribe {
                    subject.onNext(ListSpreadsheet(it))
                }
    }

    private fun reloadCategories() {
        categoryDao.findBySpreadsheetId(preferences.spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    subject.onNext(ListCategory(it))
                }
    }

    private fun downloadData() {
        spreadsheetService.list()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            val ids = it.list.map { spreadsheet -> spreadsheet.id }
                            val names = it.list.map { spreadsheet -> spreadsheet.name }

                            val idsForUpdate = spreadsheetDao.findRecordsForUpdate(ids, names)



                            spreadsheetDao.insertAll(*it.list.toTypedArray())
                        },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING_ALL) }
                )
    }

    private fun downloadCategories() {
        val spreadsheetId = preferences.spreadsheetId

        categoryService.getListBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        { categoryDao.insertAll(*it.list.toTypedArray()) },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING_CATEGORIES) }
                )
    }

    private fun handleErrors(error: Throwable, requestCode: Int) {
        val result = when (error) {
            is UserRecoverableAuthIOException ->
                StartActivity(error.intent, requestCode)
            else -> {
                ToastInfo(R.string.unknown_error, LENGTH_LONG)
            }
        }

        subject.onNext(result)
    }

    fun saveExpense(amount: Float, category: Category) {
        val spreadsheetId = preferences.spreadsheetId
        spreadsheetDao.getLocaleBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe { locale ->
                    Log.d("ExpenseViewModel", "get locale: $locale for spreadsheet: $spreadsheetId")
                    val date = currentDate(locale)
                    val expense = Expense(date, amount, "", category)

                    expenseService.save(expense, spreadsheetId)
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
}