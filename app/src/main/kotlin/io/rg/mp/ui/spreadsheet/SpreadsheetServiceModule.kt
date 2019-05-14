package io.rg.mp.ui.spreadsheet

import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import dagger.Module
import dagger.Provides
import io.rg.mp.drive.BalanceService
import io.rg.mp.drive.FolderService
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.drive.TransactionService
import io.rg.mp.persistence.dao.FailedSpreadsheetDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.ui.FragmentScope
import io.rg.mp.ui.ReloadViewAuthenticator

@Module
class SpreadsheetServiceModule {
    @Provides
    @FragmentScope
    fun spreadsheetService(drive: Drive) = SpreadsheetService(drive)

    @Provides
    @FragmentScope
    fun folderService(drive: Drive) = FolderService(drive)

    @Provides
    @FragmentScope
    fun balanceService(sheets: Sheets) = BalanceService(sheets)

    @Provides
    @FragmentScope
    fun transactionService(sheets: Sheets) = TransactionService(sheets)

    @Provides
    @FragmentScope
    fun spreadsheetViewModel(
            spreadsheetDao: SpreadsheetDao,
            folderService: FolderService,
            transactionService: TransactionService,
            spreadsheetService: SpreadsheetService,
            failedSpreadsheetDao: FailedSpreadsheetDao): SpreadsheetViewModel {
        return SpreadsheetViewModel(
                spreadsheetDao,
                folderService,
                transactionService,
                spreadsheetService,
                failedSpreadsheetDao
        )
    }

    @Provides
    @FragmentScope
    fun reloadViewAuthenticator() = ReloadViewAuthenticator()
}