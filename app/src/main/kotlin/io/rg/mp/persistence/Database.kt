package io.rg.mp.persistence

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import io.rg.mp.persistence.dao.CategoryDao
import io.rg.mp.persistence.dao.SpreadsheetDao
import io.rg.mp.persistence.entity.Category
import io.rg.mp.persistence.entity.Spreadsheet

@Database(
        entities = arrayOf(
                Category::class,
                Spreadsheet::class
        ),
        version = 1)
abstract class Database : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun spreadsheetDao(): SpreadsheetDao
}