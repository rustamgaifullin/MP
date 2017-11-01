package io.rg.mp.ui.expense

import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import dagger.Module
import dagger.Provides
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.service.sheet.CategoryRetrieverService
import io.rg.mp.service.sheet.ExpenseService

@Module
class ExpenseServiceModule {
    @Provides fun categoryService(sheets: Sheets) = CategoryRetrieverService(sheets)

    @Provides fun spreadsheetService(drive: Drive) = SpreadsheetService(drive)

    @Provides fun expenseService(sheets: Sheets) = ExpenseService(sheets)
}