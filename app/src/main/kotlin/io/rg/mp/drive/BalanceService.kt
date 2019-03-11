package io.rg.mp.drive

import com.google.api.services.sheets.v4.Sheets
import io.reactivex.Single
import io.rg.mp.drive.data.Balance
import io.rg.mp.drive.extension.extractValue
import io.rg.mp.utils.onErrorIfNotDisposed

class BalanceService(private val googleSheetService: Sheets) {
    companion object {
        private const val CURRENT_BALANCE = "I15"
        private const val PLANNED_EXPENSES = "C21"
        private const val ACTUAL_EXPENSES = "C22"
    }

    fun retrieve(spreadsheetId: String): Single<Balance> {
        return Single.create { emitter ->
            onErrorIfNotDisposed(emitter) {
                val response = googleSheetService
                        .spreadsheets()
                        .values()
                        .batchGet(spreadsheetId)
                        .setFields("valueRanges.values")
                        .setRanges(listOf(CURRENT_BALANCE, PLANNED_EXPENSES, ACTUAL_EXPENSES))
                        .execute()


                val currentBalance = response.extractValue(0, 0, 0)
                val plannedExpenses = response.extractValue(1, 0, 0)
                val actualExpenses = response.extractValue(2, 0, 0)

                emitter.onSuccess(Balance(currentBalance, actualExpenses, plannedExpenses))
            }
        }
    }
}