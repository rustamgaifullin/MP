package io.rg.mp.ui

import android.content.Intent
import io.rg.mp.persistence.entity.Category
import io.rg.mp.persistence.entity.Spreadsheet
import io.rg.mp.persistence.entity.Transaction

sealed class ViewModelResult

data class ToastInfo(val messageId: Int, val length: Int) : ViewModelResult()
data class StartActivity(val intent: Intent, val requestCode: Int) : ViewModelResult()
data class PermissionRequest(val permissions: Array<String>, val requestCode: Int) : ViewModelResult()
data class GooglePlayServicesAvailabilityError(val requestCode: Int) : ViewModelResult()

data class ListCategory(val list: List<Category>) : ViewModelResult()
data class ListSpreadsheet(val list: List<Spreadsheet>) : ViewModelResult()
data class ListTransaction(val list: List<Transaction>) : ViewModelResult()

data class SpreadsheetData(val spreadsheet: Spreadsheet) : ViewModelResult()

data class CreatedSuccessfully(val spreadsheetId: String) : ViewModelResult()

object RenamedSuccessfully : ViewModelResult()

object SavedSuccessfully : ViewModelResult()