package io.rg.mp.ui.expense

import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import dagger.Module
import dagger.Provides
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.service.sheet.CategoryService
import io.rg.mp.service.sheet.ExpenseService
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
    fun expenseService(sheets: Sheets) = ExpenseService(sheets)
}