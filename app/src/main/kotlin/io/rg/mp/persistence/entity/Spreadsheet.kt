package io.rg.mp.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spreadsheet")
data class Spreadsheet(
        @PrimaryKey var id: String,
        var name: String,
        var modifiedTime: Long,
        var locale: String = ""
)