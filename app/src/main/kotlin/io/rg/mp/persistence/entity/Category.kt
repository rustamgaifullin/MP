package io.rg.mp.persistence.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.Index

@Entity(tableName = "category",
        primaryKeys = ["name", "spreadsheet_id"],
        indices = [Index("spreadsheet_id")],
        foreignKeys = [
            ForeignKey(
                    entity = Spreadsheet::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("spreadsheet_id"),
                    onDelete = CASCADE
            )]
)
data class Category(
        @ColumnInfo(name = "name") var name: String,
        @ColumnInfo(name = "spreadsheet_id") var spreadsheetId: String
)