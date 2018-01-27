package io.rg.mp.utils

import io.rg.mp.ui.expense.model.DateInt
import java.text.DateFormat
import java.util.Calendar

fun formatDate(localeString: String, date: DateInt): String {
    val (year, month, dayOfMonth) = date

    val calendar = Calendar.getInstance().apply {
        set(year, month, dayOfMonth)
    }

    val locale = createLocale(localeString)
    val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale)
    return dateFormat.format(calendar.time)
}

fun Calendar.day() = get(Calendar.DAY_OF_MONTH)
fun Calendar.month() = get(Calendar.MONTH)
fun Calendar.year() = get(Calendar.YEAR)