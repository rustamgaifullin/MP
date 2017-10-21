package io.rg.mp.persistence.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.rg.mp.persistence.entity.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category")
    fun all() : Flowable<List<Category>>

    @Query("SELECT * FROM category where spreadsheet_id = :spreadsheetId")
    fun findBySpreadsheetId(spreadsheetId: String): Flowable<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg categories: Category)
}