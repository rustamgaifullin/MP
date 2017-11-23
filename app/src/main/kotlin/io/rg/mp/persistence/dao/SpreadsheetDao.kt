package io.rg.mp.persistence.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.rg.mp.persistence.entity.Spreadsheet

@Dao
interface SpreadsheetDao {
    @Query("SELECT * FROM spreadsheet")
    fun all() : Flowable<List<Spreadsheet>>

    @Query("SELECT locale FROM spreadsheet WHERE id = :spreadsheetId")
    fun getLocaleBy(spreadsheetId: String): Flowable<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg spreadsheets: Spreadsheet)

    @Query("UPDATE spreadsheet SET locale = :locale WHERE id = :spreadsheetId")
    fun updateLocale(locale: String, spreadsheetId: String)
}