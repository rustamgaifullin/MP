package io.rg.mp.persistence.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index

@Entity(tableName = "category",
        primaryKeys = arrayOf("name", "spreadsheet_id"),
        indices = arrayOf(
                Index("spreadsheet_id")
        ),
        foreignKeys = arrayOf(
                ForeignKey(
                        entity = Spreadsheet::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("spreadsheet_id")
                )
        )
)
data class Category(
        @ColumnInfo(name = "name") var name: String,
        @ColumnInfo(name = "spreadsheet_id") var spreadsheetId: String
)