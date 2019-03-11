package io.rg.mp.drive

import com.google.api.services.sheets.v4.Sheets
import io.reactivex.Single
import io.rg.mp.utils.onErrorIfNotDisposed

class LocaleService(private val googleSheetService: Sheets) {
    fun getBy(spreadsheetId: String): Single<String> {
        return Single.create<String> { emitter ->
            onErrorIfNotDisposed(emitter) {
                val response = googleSheetService
                        .spreadsheets()
                        .get(spreadsheetId)
                        .setIncludeGridData(true)
                        .setRanges(listOf("A1:A1"))
                        .setFields("properties.locale")
                        .execute()

                val locale = response.properties.locale

                if (!locale.isNullOrEmpty()) {
                    emitter.onSuccess(locale)
                }
            }
        }
    }
}