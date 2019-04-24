package io.rg.mp.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable
import io.rg.mp.persistence.entity.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category")
    fun all() : Flowable<List<Category>>

    @Query("SELECT * FROM category WHERE spreadsheet_id = :spreadsheetId")
    fun findBySpreadsheetId(spreadsheetId: String): Flowable<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg categories: Category)
}