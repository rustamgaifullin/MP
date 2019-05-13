package io.rg.mp.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Single
import io.rg.mp.persistence.entity.FailedSpreadsheet

@Dao
interface FailedSpreadsheetDao {
    @Query("SELECT * FROM failedSpreadsheet")
    fun all() : Single<List<FailedSpreadsheet>>

    @Query("DELETE FROM failedSpreadsheet WHERE spreadsheetId = :spreadsheetId")
    fun delete(spreadsheetId: String)

    @Insert
    fun insert(vararg failedSpreadsheets: FailedSpreadsheet)
}