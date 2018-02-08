package io.rg.mp.drive

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.BatchClearValuesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import io.reactivex.Completable
import io.reactivex.Flowable
import io.rg.mp.drive.data.Expense
import io.rg.mp.drive.data.NotSaved
import io.rg.mp.drive.data.Result
import io.rg.mp.drive.data.Saved


class TransactionService(private val googleSheetService: Sheets) {
    companion object {
        private const val EXPENSES_RANGE = "Transactions!B5:E5"
        private const val INCOMES_RANGE = "Transactions!G5:J5"
    }

    fun saveExpense(data: Expense, spreadsheetId: String): Flowable<Result> {
        return Flowable.fromCallable {
            val body = ValueRange().setValues(listOf(data.asCellsList()))

            val result = googleSheetService
                    .spreadsheets()
                    .values()
                    .append(spreadsheetId, EXPENSES_RANGE, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute()

            if (result.tableRange.isNotEmpty()) {
                Saved()
            } else {
                NotSaved()
            }
        }
    }

    fun clearAllTransactions(spreadsheetId: String): Completable {
        return Completable.fromAction {
            val request = BatchClearValuesRequest()
            request.ranges = listOf(EXPENSES_RANGE, INCOMES_RANGE)

            googleSheetService
                    .spreadsheets()
                    .values()
                    .batchClear(spreadsheetId, request)
                    .execute()
        }
    }
}