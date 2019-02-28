package io.rg.mp.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.Flowable
import io.reactivex.Single
import io.rg.mp.persistence.entity.Spreadsheet

@Dao
abstract class SpreadsheetDao {
    @Query("SELECT * FROM spreadsheet ORDER BY modifiedTime DESC")
    abstract fun allSorted() : Flowable<List<Spreadsheet>>

    @Query("SELECT locale FROM spreadsheet WHERE id = :spreadsheetId LIMIT 1")
    abstract fun getLocaleBy(spreadsheetId: String): Single<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertAll(vararg spreadsheets: Spreadsheet)

    @Query("SELECT id FROM spreadsheet WHERE name NOT IN (:names) AND id IN (:ids)")
    abstract fun findRecordsForUpdate(ids: List<String>, names: List<String>): List<String>

    @Query("SELECT id FROM spreadsheet WHERE id NOT IN (:ids)")
    abstract fun findRecordsForDelete(ids: List<String>): List<String>

    @Update
    abstract fun updateSpreadsheets(vararg spreadsheets: Spreadsheet)

    @Query("UPDATE spreadsheet SET locale = :locale WHERE id = :spreadsheetId")
    abstract fun updateLocale(locale: String, spreadsheetId: String): Int

    @Query("DELETE FROM spreadsheet WHERE id IN (:spreadsheetId)")
    abstract fun deleteByIds(spreadsheetId: List<String>)

    @Transaction
    open fun updateData(spreadsheetList: List<Spreadsheet>) {
        val ids = spreadsheetList.map { spreadsheet -> spreadsheet.id }
        val names = spreadsheetList.map { spreadsheet -> spreadsheet.name }

        val idsToDelete = findRecordsForDelete(ids)
        val idsToUpdate = findRecordsForUpdate(ids, names)

        deleteByIds(idsToDelete)
        updateByIds(idsToUpdate, spreadsheetList)
        insertAll(*spreadsheetList.toTypedArray())
    }

    private fun updateByIds(ids: List<String>, spreadsheets:List<Spreadsheet>) {
        val spreadsheetsToUpdate = ids
                .map { entry ->
                    spreadsheets.first { spreadsheet ->
                        entry == spreadsheet.id
                    }
                }
        updateSpreadsheets(*spreadsheetsToUpdate.toTypedArray())
    }
}