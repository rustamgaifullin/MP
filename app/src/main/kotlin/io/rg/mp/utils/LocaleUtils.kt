package io.rg.mp.utils

import java.util.Locale

fun getLocaleInstance(locale: String = ""): Locale {
    if (locale.isEmpty()) return Locale.getDefault()

    val splitList = locale.split("_")
    return if (splitList.size == 2) {
        val language = splitList[0]
        val country = splitList[1]

        Locale(language, country)
    } else {
        Locale(locale)
    }
}