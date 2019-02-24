package io.rg.mp.ui.spreadsheet

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.rg.mp.drive.CopyService
import io.rg.mp.drive.FolderService
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.drive.TransactionService
import io.rg.mp.drive.data.CreationResult
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.ui.AbstractViewModel
import io.rg.mp.ui.CreatedSuccessfully
import io.rg.mp.ui.ListSpreadsheet

class SpreadsheetViewModel(
        private val spreadsheetDao: SpreadsheetDao,
        private val copyService: CopyService,
        private val folderService: FolderService,
        private val transactionService: TransactionService,
        private val spreadsheetService: SpreadsheetService) : AbstractViewModel() {

    companion object {
        const val REQUEST_AUTHORIZATION_LOADING_SPREADSHEETS = 2001
        const val REQUEST_AUTHORIZATION_NEW_SPREADSHEET = 2003
    }

    fun reloadData() {
        reloadSpreadsheets()
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

    private fun downloadSpreadsheets() {
        val disposable = spreadsheetService.list()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { (spreadsheetList) -> spreadsheetDao.updateData(spreadsheetList) },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING_SPREADSHEETS) }
                )
        compositeDisposable.add(disposable)
    }

    fun createNewSpreadsheet() {
        val disposable = copyService
                .copy()
                .flatMap(this@SpreadsheetViewModel::moveToFolderAndClearTransactions)
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
}