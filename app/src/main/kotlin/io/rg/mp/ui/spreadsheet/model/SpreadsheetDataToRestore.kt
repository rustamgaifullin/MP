package io.rg.mp.ui.spreadsheet.model

import android.os.Parcelable
import io.rg.mp.ui.spreadsheet.SpreadsheetViewModel.Companion.DEFAULT_TEMPLATE_ID
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SpreadsheetDataToRestore(val name: String, val id: String): Parcelable {
    companion object {
        fun emptyPair() = Pair("", DEFAULT_TEMPLATE_ID)
    }

    fun getNameIdPair() = Pair(name, id)
}