package io.rg.mp.utils

import java.text.DateFormat
import java.util.Date

fun currentDate(localeString: String): String {
    val locale = createLocale(localeString)
    val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale)
    return dateFormat.format(Date())
}