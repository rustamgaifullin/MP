package io.rg.mp.drive

import com.google.api.services.sheets.v4.Sheets
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.rg.mp.utils.onErrorIfNotCancelled

class LocaleService(private val googleSheetService: Sheets) {
    fun getBy(spreadsheetId: String): Flowable<String> {
        return Flowable.create ({ emitter ->
            onErrorIfNotCancelled(emitter) {
                val response = googleSheetService
                        .spreadsheets()
                        .get(spreadsheetId)
                        .setIncludeGridData(true)
                        .setRanges(listOf("A1:A1"))
                        .setFields("properties.locale")
                        .execute()

                val locale = response.properties.locale

                if (!locale.isNullOrEmpty()) {
                    emitter.onNext(locale)
                }

                emitter.onComplete()
            }
        }, BackpressureStrategy.BUFFER)
    }
}