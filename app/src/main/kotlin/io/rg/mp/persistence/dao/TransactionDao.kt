package io.rg.mp.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable
import io.rg.mp.persistence.entity.Transaction

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE spreadsheet_id = :spreadsheetId ORDER BY date DESC")
    fun findBySpreadsheetIdSorted(spreadsheetId: String) : Flowable<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg transactions: Transaction)
}