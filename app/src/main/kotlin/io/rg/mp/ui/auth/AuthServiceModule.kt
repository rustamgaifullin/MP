package io.rg.mp.ui.auth

import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import dagger.Module
import dagger.Provides
import io.rg.mp.service.drive.SpreadsheetService
import io.rg.mp.service.sheet.CategoryService
import io.rg.mp.ui.FragmentScope

@Module
class AuthServiceModule {
    @Provides
    @FragmentScope
    fun categoryService(sheets: Sheets) = CategoryService(sheets)

    @Provides
    @FragmentScope
    fun spreadsheetService(drive: Drive) = SpreadsheetService(drive)
}