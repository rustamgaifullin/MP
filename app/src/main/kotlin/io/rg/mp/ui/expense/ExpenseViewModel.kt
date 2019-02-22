package io.rg.mp.ui.expense

import android.util.Log
import android.widget.Toast.LENGTH_LONG
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.rg.mp.R
import io.rg.mp.drive.BalanceService
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
import io.rg.mp.ui.DisposableViewModel
import io.rg.mp.ui.expense.model.DateInt
import io.rg.mp.ui.model.BalanceUpdated
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
        private val balanceService: BalanceService,
        private val categoryDao: CategoryDao,
        private val spreadsheetDao: SpreadsheetDao,
        private val preferences: Preferences) : DisposableViewModel {

    companion object {
        const val REQUEST_AUTHORIZATION_EXPENSE = 2000
        const val REQUEST_AUTHORIZATION_LOADING_ALL = 2001
        const val REQUEST_AUTHORIZATION_LOADING_CATEGORIES = 2002
        const val REQUEST_AUTHORIZATION_NEW_SPREADSHEET = 2003
    }

    private val subject = PublishSubject.create<ViewModelResult>()
    private var lastDate = DateInt.currentDateInt()
    private val progressSubject = BehaviorSubject.createDefault(0)
    private val compositeDisposable = CompositeDisposable()

    override fun clear() {
        compositeDisposable.dispose()
    }

    fun viewModelNotifier(): Flowable<ViewModelResult> = subject.toFlowable(BackpressureStrategy.BUFFER)

    fun currentSpreadsheet(spreadsheetList: List<Spreadsheet>): Int =
            spreadsheetList.indexOfFirst { (id) -> id == preferences.spreadsheetId }

    fun onSpreadsheetItemSelected(spreadsheetId: String) {
        preferences.spreadsheetId = spreadsheetId

        reloadCategories()
        downloadDataFor(spreadsheetId)
    }

    fun loadCurrentCategories() {
        if (preferences.isSpreadsheetIdAvailable) {
            val spreadsheetId = preferences.spreadsheetId

            downloadDataFor(spreadsheetId)
        }
    }

    private fun downloadDataFor(spreadsheetId: String) {
        downloadCategories(spreadsheetId)
        updateLocale(spreadsheetId)
        reloadBalance(spreadsheetId)
    }

    private fun updateLocale(spreadsheetId: String) {
        val disposable = localeService.getBy(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { progressSubject.onNext(1) }
                .doFinally { progressSubject.onNext(-1) }
                .subscribe(
                        {
                            Log.d("ExpenseViewModel", "update locale: $it for spreadsheet: $spreadsheetId")
                            val result = spreadsheetDao.updateLocale(it, spreadsheetId)
                            Log.d("ExpenseViewModel", "result code: $result")
                        },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING_CATEGORIES) }
                )
        compositeDisposable.add(disposable)
    }

    fun startLoadingData() {
        reloadSpreadsheets()
        reloadCategories()

        downloadSpreadsheets()
    }

    private fun reloadSpreadsheets() {
        val disposable = spreadsheetDao.all()
                .subscribeOn(Schedulers.io())
                .subscribe {
                    subject.onNext(ListSpreadsheet(it))
                }
        compositeDisposable.add(disposable)
    }

    private fun reloadCategories() {
        val disposable = categoryDao.findBySpreadsheetId(preferences.spreadsheetId)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    subject.onNext(ListCategory(it))
                }
        compositeDisposable.add(disposable)
    }

    private fun reloadBalance(spreadsheetId: String) {
        val disposable = balanceService.retrieve(spreadsheetId)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { progressSubject.onNext(1) }
                .doFinally { progressSubject.onNext(-1) }
                .subscribe { balance ->
                    subject.onNext(BalanceUpdated(balance))
                }
        compositeDisposable.add(disposable)
    }

    private fun downloadSpreadsheets() {
        val disposable = spreadsheetService.list()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { (spreadsheetList) ->
                            spreadsheetDao.updateData(spreadsheetList)
                        },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING_ALL) }
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
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING_CATEGORIES) }
                )
        compositeDisposable.add(disposable)
    }

    private fun handleErrors(error: Throwable, requestCode: Int) {
        val result = when (error) {
            is UserRecoverableAuthIOException ->
                StartActivity(error.intent, requestCode)
            else -> {
                Log.e("RUSTA", error.message, error)
                ToastInfo(R.string.unknown_error, LENGTH_LONG)
            }
        }

        subject.onNext(result)
    }

    fun saveExpense(amount: Float, category: Category, description: String) {
        val spreadsheetId = preferences.spreadsheetId
        val disposable = spreadsheetDao.getLocaleBy(spreadsheetId)
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
        compositeDisposable.add(disposable)
    }

    private fun handleSaving(result: Result) {
        val messageId = when (result) {
            is Saved -> {
                subject.onNext(SavedSuccessfully())
                reloadBalance(preferences.spreadsheetId)
                R.string.saved_message
            }
            is NotSaved -> R.string.not_saved_message
        }

        subject.onNext(ToastInfo(messageId, LENGTH_LONG))
    }

    fun updateDate(newDate: DateInt) {
        lastDate = newDate

        val disposable = spreadsheetDao.getLocaleBy(preferences.spreadsheetId)
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
        compositeDisposable.add(disposable)
    }

    fun lastDate() = lastDate

    fun createNewSpreadsheet() {
        val disposable = copyService
                .copy()
                .flatMap(this@ExpenseViewModel::moveToFolderAndClearTransactions)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { subject.onNext(CreatedSuccessfully(it.id)) },
                        { handleErrors(it, REQUEST_AUTHORIZATION_NEW_SPREADSHEET) }
                )
        compositeDisposable.add(disposable)
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

    fun isOperationInProgress(): Observable<Boolean> = progressSubject
            .scan { sum, item -> sum + item }
            .map { sum -> sum > 0 }
}