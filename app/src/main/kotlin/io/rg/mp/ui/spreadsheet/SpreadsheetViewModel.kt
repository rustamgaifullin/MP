package io.rg.mp.ui.spreadsheet

import android.os.Bundle
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
import io.rg.mp.utils.getLocaleInstance
import java.text.SimpleDateFormat
import java.util.*

class SpreadsheetViewModel(
        private val spreadsheetDao: SpreadsheetDao,
        private val copyService: CopyService,
        private val folderService: FolderService,
        private val transactionService: TransactionService,
        private val spreadsheetService: SpreadsheetService) : AbstractViewModel() {

    companion object {
        const val REQUEST_AUTHORIZATION_LOADING_SPREADSHEETS = 2001
        const val REQUEST_AUTHORIZATION_NEW_SPREADSHEET = 2003
        const val SPREADSHEET_NAME = "spreadsheetName"
    }

    fun reloadData() {
        reloadSpreadsheets()
        downloadSpreadsheets()
    }

    private fun reloadSpreadsheets() {
        val disposable = spreadsheetDao.allSorted()
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

    fun createNewSpreadsheet(name: String) {
        val disposable = copyService
                .copy(name)
                .flatMap(this@SpreadsheetViewModel::moveToFolderAndClearTransactions)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        { subject.onNext(CreatedSuccessfully(it.id)) },
                        {
                            val extras = Bundle()
                            extras.putString(SPREADSHEET_NAME, name)
                            handleErrors(it, REQUEST_AUTHORIZATION_NEW_SPREADSHEET, extras)
                        }
                )
        compositeDisposable.add(disposable)
    }

    //FIXME deleting spreadsheet in doOnError is not correct if you're not authorized
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

    fun createSpreadsheetName() : String {
        val simpleDateFormat = SimpleDateFormat("LLLL YYYY", getLocaleInstance())
        return simpleDateFormat.format(Date())
    }
}