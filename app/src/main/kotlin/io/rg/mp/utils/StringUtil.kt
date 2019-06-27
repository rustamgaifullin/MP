package io.rg.mp.utils

fun String.extractDouble(): Double {
    val regex = """^-?[0-9][0-9,\\.]+""".toRegex()
    val number = regex.find(this)?.value ?: "0"

    return number.replace(",", ".") .toDouble()
}