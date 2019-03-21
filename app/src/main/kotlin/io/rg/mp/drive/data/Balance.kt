package io.rg.mp.drive.data

import io.rg.mp.persistence.entity.Spreadsheet

data class Balance(val current: String = "", val actual: String = "", val planned: String = "") {
    companion object {
        fun fromSpreadsheet(spreadsheet: Spreadsheet): Balance {
            return Balance(
                    spreadsheet.currentBalance,
                    spreadsheet.actualExpense,
                    spreadsheet.plannedExpense)
        }
    }
}