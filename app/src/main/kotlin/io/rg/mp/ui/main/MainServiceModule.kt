package io.rg.mp.ui.main

import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import dagger.Module
import dagger.Provides
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.service.sheet.CategoryRetrieverService

@Module
class MainServiceModule {
    @Provides fun categoryService(
            googleSheetService: Sheets) = CategoryRetrieverService(googleSheetService)

    @Provides fun spreadsheetService(drive: Drive) = SpreadsheetService(drive)
}