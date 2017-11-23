package io.rg.mp.persistence.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "spreadsheet")
data class Spreadsheet(
        @PrimaryKey var id: String,
        var name: String,
        var locale: String = ""
)