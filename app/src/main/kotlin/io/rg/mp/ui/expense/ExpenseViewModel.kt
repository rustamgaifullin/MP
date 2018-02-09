package io.rg.mp.ui.expense

import android.util.Log
import android.widget.Toast.LENGTH_LONG
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.rg.mp.R
import io.rg.mp.drive.CategoryService
import io.rg.mp.drive.CopyService
import io.rg.mp.drive.FolderService
import io.rg.mp.drive.LocaleService
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.drive.TransactionService
import io.rg.mp.drive.data.CreationResult
import io.rg.mp.drive.data.Expense
import io.rg.mp.drive.data.NotSaved
import io.rg.mp.drive.data.Result
import io.rg.mp.drive.data.Saved
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.persistence.entity.Category
import io.rg.mp.persistence.entity.Spreadsheet
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
import io.rg.mp.utils.formatDate

class ExpenseViewModel(
        private val categoryService: CategoryService,
        private val spreadsheetService: SpreadsheetService,
        private val localeService: LocaleService,
        private val transactionService: TransactionService,
        private val copyService: CopyService,
        private val folderService: FolderService,
        private val categoryDao: CategoryDao,
        private val spreadsheetDao: SpreadsheetDao,
        private val preferences: Preferences) {

    companion object {
        const val REQUEST_AUTHORIZATION_EXPENSE = 2000
        const val REQUEST_AUTHORIZATION_LOADING_ALL = 2001
        const val REQUEST_AUTHORIZATION_LOADING_CATEGORIES = 2002
        const val REQUEST_AUTHORIZATION_NEW_SPREADSHEET = 2003
    }

    private val subject = PublishSubject.create<ViewModelResult>()

    private var lastDate = DateInt.currentDateInt()

    fun viewModelNotifier(): Flowable<ViewModelResult> = subject.toFlowable(BackpressureStrategy.BUFFER)

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
                        { (spreadsheetList) ->
                            spreadsheetDao.updateData(spreadsheetList)
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

    fun saveExpense(amount: Float, category: Category, description: String) {
        val spreadsheetId = preferences.spreadsheetId
        spreadsheetDao.getLocaleBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe { locale ->
                    val date = formatDate(locale, lastDate)
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

    fun updateDate(newDate: DateInt) {
        spreadsheetDao.getLocaleBy(preferences.spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe { locale ->
                    lastDate = newDate

                    val date = formatDate(locale, lastDate)

                    subject.onNext(DateChanged(date))
                }
    }

    fun lastDate() = lastDate

    fun createNewSpreadsheet() {
        copyService
                .copy()
                .flatMap(this@ExpenseViewModel::moveToFolderAndClearTransactions)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { subject.onNext(CreatedSuccessfully(it.id)) },
                        { handleErrors(it, REQUEST_AUTHORIZATION_NEW_SPREADSHEET) }
                )
    }

    private fun moveToFolderAndClearTransactions(result: CreationResult): Single<CreationResult> {
        return folderService
                .folderIdForCurrentYear()
                .flatMap { folderId ->
                    Completable.concat(
                            listOf(
                                    folderService.moveToFolder(result.id, folderId),
                                    transactionService.clearAllTransactions(result.id)
                            ))
                            .toSingleDefault(result)
                }
                .doOnError { spreadsheetService.deleteSpreadsheet(result.id).subscribe() }
    }
}