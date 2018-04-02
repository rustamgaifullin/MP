package io.rg.mp.ui.expense

import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import dagger.Module
import dagger.Provides
import io.rg.mp.drive.BalanceService
import io.rg.mp.drive.CategoryService
import io.rg.mp.drive.CopyService
import io.rg.mp.drive.FolderService
import io.rg.mp.drive.LocaleService
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.drive.TransactionService
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.ui.FragmentScope
import io.rg.mp.utils.Preferences

@Module
class ExpenseServiceModule {
    @Provides
    @FragmentScope
    fun categoryService(sheets: Sheets) = CategoryService(sheets)

    @Provides
    @FragmentScope
    fun spreadsheetService(drive: Drive) = SpreadsheetService(drive)

    @Provides
    @FragmentScope
    fun localeService(sheets: Sheets) = LocaleService(sheets)

    @Provides
    @FragmentScope
    fun transactionService(sheets: Sheets) = TransactionService(sheets)

    @Provides
    @FragmentScope
    fun copyService(sheets: Sheets) = CopyService(sheets)

    @Provides
    @FragmentScope
    fun folderService(drive: Drive) = FolderService(drive)

    @Provides
    @FragmentScope
    fun balanceService(sheets: Sheets) = BalanceService(sheets)

    @Provides
    @FragmentScope
    fun expenseViewModel(
            categoryService: CategoryService,
            spreadsheetService: SpreadsheetService,
            localeService: LocaleService,
            transactionService: TransactionService,
            copyService: CopyService,
            folderService: FolderService,
            balanceService: BalanceService,
            categoryDao: CategoryDao,
            spreadsheetDao: SpreadsheetDao,
            preferences: Preferences) =
            ExpenseViewModel(categoryService,
                    spreadsheetService,
                    localeService,
                    transactionService,
                    copyService,
                    folderService,
                    balanceService,
                    categoryDao,
                    spreadsheetDao,
                    preferences)
}