package io.rg.mp.ui.spreadsheet

import android.os.Bundle
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.rg.mp.drive.CopyService
import io.rg.mp.drive.FolderService
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.drive.TransactionService
import io.rg.mp.drive.data.CreationResult
import io.rg.mp.persistence.dao.FailedSpreadsheetDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.persistence.entity.FailedSpreadsheet
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
        private val spreadsheetService: SpreadsheetService,
        private val failedSpreadsheetDao: FailedSpreadsheetDao) : AbstractViewModel() {

    companion object {
        const val REQUEST_AUTHORIZATION_LOADING_SPREADSHEETS = 2001
        const val REQUEST_AUTHORIZATION_NEW_SPREADSHEET = 2003
        const val REQUEST_AUTHORIZATION_FOR_DELETE = 2005
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
                .doOnSubscribe { progressSubject.onNext(1) }
                .doFinally { progressSubject.onNext(-1) }
                .subscribe(
                        { (spreadsheetList) -> spreadsheetDao.updateData(spreadsheetList) },
                        { handleErrors(it, REQUEST_AUTHORIZATION_LOADING_SPREADSHEETS) }
                )
        compositeDisposable.add(disposable)
    }

    fun createNewSpreadsheet(name: String) {
        val disposable = copyService
                .copy(name)
                .doOnSuccess(temporaryInsertToFailed())
                .flatMap(this@SpreadsheetViewModel::moveToFolderAndClearTransactions)
                .doOnSubscribe { progressSubject.onNext(1) }
                .doFinally { progressSubject.onNext(-1) }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            failedSpreadsheetDao.delete(it.id)
                            subject.onNext(CreatedSuccessfully(it.id))
                        },
                        {
                            val extras = Bundle()
                            extras.putString(SPREADSHEET_NAME, name)
                            handleErrors(it, REQUEST_AUTHORIZATION_NEW_SPREADSHEET, extras)
                        }
                )
        compositeDisposable.add(disposable)
    }

    private fun temporaryInsertToFailed(): (CreationResult) -> Unit =
            { failedSpreadsheetDao.insert(FailedSpreadsheet(spreadsheetId = it.id)) }

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
    }

    fun deleteFailedSpreadsheets() {
        val disposable = failedSpreadsheetDao.all()
                .flatMapPublisher { Flowable.fromIterable(it) }
                .subscribeOn(Schedulers.io())
                .subscribe { failedSpreadsheet ->
                    spreadsheetService.deleteSpreadsheet(failedSpreadsheet.spreadsheetId)
                            .subscribe(
                                    {
                                        spreadsheetDao.delete(failedSpreadsheet.spreadsheetId)
                                        failedSpreadsheetDao.delete(failedSpreadsheet.spreadsheetId)
                                    },
                                    { handleErrors(it, REQUEST_AUTHORIZATION_FOR_DELETE) })

                }
        compositeDisposable.add(disposable)
    }

    fun createSpreadsheetName(): String {
        val simpleDateFormat = SimpleDateFormat("LLLL YYYY", getLocaleInstance())
        return simpleDateFormat.format(Date())
    }
}