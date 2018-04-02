package io.rg.mp.drive.extension

import com.google.api.services.sheets.v4.model.BatchGetValuesResponse

fun BatchGetValuesResponse.extractValue(rangeIndex: Int,
                                        rowIndex: Int,
                                        columnIndex: Int): String {

    return valueRanges
            ?.getIfExist(rangeIndex)    //get from range if not null and index within list
            ?.getValues()
            ?.getIfExist(rowIndex)      //get row if not null and index within list
            ?.getIfExist(columnIndex)   //get column if not null and index within list
            ?.toString()                //return value
            ?: ""                       //otherwise default
}

fun <T> List<T>.getIfExist(index: Int): T? {
    if (size > index) return this[index]

    return null
}