package io.rg.mp.drive

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import io.reactivex.Flowable
import io.rg.mp.drive.data.Expense
import io.rg.mp.drive.data.NotSaved
import io.rg.mp.drive.data.Result
import io.rg.mp.drive.data.Saved


class ExpenseService(private val googleSheetService: Sheets) {
    fun save(data: Expense, spreadsheetId: String): Flowable<Result> {
        return Flowable.fromCallable {
            val body = ValueRange().setValues(listOf(data.asCellsList()))

            val result = googleSheetService
                    .spreadsheets()
                    .values()
                    .append(spreadsheetId, "Transactions!B5:E5", body)
                    .setValueInputOption("USER_ENTERED")
                    .execute()

            if (result.tableRange.isNotEmpty()) {
                Saved()
            } else {
                NotSaved()
            }
        }
    }
}