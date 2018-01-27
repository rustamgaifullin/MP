package io.rg.mp.ui.expense.model

import android.annotation.SuppressLint
import android.os.Parcelable
import io.rg.mp.utils.day
import io.rg.mp.utils.month
import io.rg.mp.utils.year
import kotlinx.android.parcel.Parcelize
import java.util.Calendar

@SuppressLint("ParcelCreator")
@Parcelize
data class DateInt(val year: Int, val month: Int, val dayOfMonth: Int): Parcelable {
    companion object {
        fun currentDateInt(): DateInt {
            val calendar = Calendar.getInstance()
            return DateInt(calendar.year(), calendar.month(), calendar.day())
        }
    }
}