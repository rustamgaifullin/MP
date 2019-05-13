package io.rg.mp.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.FailedSpreadsheetDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.persistence.dao.TransactionDao
import io.rg.mp.persistence.entity.Category
import io.rg.mp.persistence.entity.FailedSpreadsheet
import io.rg.mp.persistence.entity.Spreadsheet
import io.rg.mp.persistence.entity.Transaction

@Database(
        entities = [
            (Category::class), (Spreadsheet::class), (Transaction::class), (FailedSpreadsheet::class)
        ],
        version = 1)
abstract class Database : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun spreadsheetDao(): SpreadsheetDao
    abstract fun transactionDao(): TransactionDao
    abstract fun failedSpreadsheetDao(): FailedSpreadsheetDao
}