package io.rg.mp.ui.expense

import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import dagger.Module
import dagger.Provides
import io.rg.mp.drive.BalanceService
import io.rg.mp.drive.CategoryService
import io.rg.mp.drive.LocaleService
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.drive.TransactionService
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.ui.FragmentScope

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
    fun balanceService(sheets: Sheets) = BalanceService(sheets)


    @Provides
    @FragmentScope
    fun expenseViewModel(
            categoryService: CategoryService,
            localeService: LocaleService,
            transactionService: TransactionService,
            balanceService: BalanceService,
            categoryDao: CategoryDao,
            spreadsheetDao: SpreadsheetDao) : ExpenseViewModel {
        return ExpenseViewModel(categoryService,
                localeService,
                transactionService,
                balanceService,
                categoryDao,
                spreadsheetDao)
    }
}
