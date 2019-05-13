package io.rg.mp.ui.auth

import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import dagger.Module
import dagger.Provides
import io.rg.mp.drive.CategoryService
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.ui.FragmentScope
import io.rg.mp.utils.GoogleApiAvailabilityService
import io.rg.mp.utils.Preferences

@Module
class AuthServiceModule {
    @Provides
    @FragmentScope
    fun categoryService(sheets: Sheets) = CategoryService(sheets)

    @Provides
    @FragmentScope
    fun spreadsheetService(drive: Drive) = SpreadsheetService(drive)

    @Provides
    @FragmentScope
    fun authViewModel(
            context: Context,
            googleApiAvailabilityService: GoogleApiAvailabilityService,
            credential: GoogleAccountCredential,
            preferences: Preferences,
            spreadsheetService: SpreadsheetService): AuthViewModel {

        return AuthViewModel(
                context, googleApiAvailabilityService, credential, preferences, spreadsheetService)
    }
}