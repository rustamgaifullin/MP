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
import io.rg.mp.drive.data.TransactionList
import io.rg.mp.persistence.entity.Transaction


class TransactionService(private val googleSheetService: Sheets) {
    companion object {
        private const val EXPENSES_RANGE = "Transactions!B5:E5"
        private const val EXPENSES_RANGE_ALL = "Transactions!B5:\$E"
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
                Saved(spreadsheetId)
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

    fun all(spreadsheetId: String): Flowable<TransactionList> {
        return Flowable.fromCallable {
            val response = googleSheetService
                    .spreadsheets()
                    .values()
                    .get(spreadsheetId, EXPENSES_RANGE_ALL)
                    .execute()

            var transactionList = emptyList<Transaction>()

            if (response.getValues() != null) {
                transactionList = response.getValues()
                        .filter { it.size > 0 }
                        .map {
                            Transaction(
                                    0L,
                                    it[0].toString(),
                                    it[1].toString(),
                                    it[2].toString(),
                                    it[3].toString(),
                                    spreadsheetId)
                        }
            }

            TransactionList(transactionList)
        }
    }
}