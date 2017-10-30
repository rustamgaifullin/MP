package io.rg.mp.service.sheet

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import io.reactivex.Flowable
import io.rg.mp.service.data.Expense
import io.rg.mp.service.data.NotSaved
import io.rg.mp.service.data.Result
import io.rg.mp.service.data.Saved


class ExpenseService(private val googleSheetService: Sheets) {
    fun save(data: Expense, spreadsheetId: String): Flowable<Result> {
        return Flowable.fromCallable {
            val body = ValueRange().setValues(listOf(data.asCellsList()))

            val result = googleSheetService
                    .spreadsheets()
                    .values()
                    .append(spreadsheetId, "Transactions!B6:E6", body)
                    .execute()

            if (result.tableRange.isNotEmpty()) {
                Saved()
            } else {
                NotSaved()
            }
        }
    }
}