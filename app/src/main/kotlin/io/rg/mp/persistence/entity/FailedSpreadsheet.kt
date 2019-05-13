package io.rg.mp.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "failedSpreadsheet")
data class FailedSpreadsheet(
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        var spreadsheetId: String = "")