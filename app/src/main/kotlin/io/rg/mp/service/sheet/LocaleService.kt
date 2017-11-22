package io.rg.mp.service.sheet

import com.google.api.services.sheets.v4.Sheets
import io.reactivex.Flowable

class LocaleService(private val googleSheetService: Sheets) {
    fun getBy(spreadsheetId: String): Flowable<String> {
        return Flowable.fromCallable {
            val response = googleSheetService
                    .spreadsheets()
                    .get(spreadsheetId)
                    .setIncludeGridData(true)
                    .setRanges(listOf("A1:A1"))
                    .setFields("properties.locale")
                    .execute()

            response.properties.locale
        }
    }
}