package io.rg.mp.persistence.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "transactions",
        indices = [Index("spreadsheet_id")],
        foreignKeys = [
            ForeignKey(
                    entity = Spreadsheet::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("spreadsheet_id"),
                    onDelete = ForeignKey.CASCADE
            )]
)
data class Transaction(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Long,
        @ColumnInfo(name = "date") var date: String,
        @ColumnInfo(name = "amount") var amount: String,
        @ColumnInfo(name = "description") var description: String,
        @ColumnInfo(name = "category") var category: String,
        @ColumnInfo(name = "spreadsheet_id") var spreadsheetId: String
)