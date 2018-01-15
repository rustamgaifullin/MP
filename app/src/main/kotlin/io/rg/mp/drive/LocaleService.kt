package io.rg.mp.drive

import com.google.api.services.sheets.v4.Sheets
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

class LocaleService(private val googleSheetService: Sheets) {
    fun getBy(spreadsheetId: String): Flowable<String> {
        return Flowable.create({
            val response = googleSheetService
                    .spreadsheets()
                    .get(spreadsheetId)
                    .setIncludeGridData(true)
                    .setRanges(listOf("A1:A1"))
                    .setFields("properties.locale")
                    .execute()

            val locale = response.properties.locale

            if (!locale.isNullOrEmpty()) {
                it.onNext(locale)
            }

            it.onComplete()
        }, BackpressureStrategy.BUFFER)
    }
}