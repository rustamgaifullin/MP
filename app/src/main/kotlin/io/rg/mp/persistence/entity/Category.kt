package io.rg.mp.persistence.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index

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
        @ColumnInfo var planned: String,
        @ColumnInfo var actual: String,
        @ColumnInfo var difference: String,
        @ColumnInfo(name = "spreadsheet_id") var spreadsheetId: String
)