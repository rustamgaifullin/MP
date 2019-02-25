package io.rg.mp.persistence.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.rg.mp.persistence.entity.Transaction

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE spreadsheet_id = :spreadsheetId ORDER BY date")
    fun findBySpreadsheetIdSorted(spreadsheetId: String) : Flowable<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg transactions: Transaction)
}