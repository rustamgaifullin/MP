package io.rg.mp.drive

import com.google.api.services.drive.Drive
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.rg.mp.persistence.entity.Spreadsheet

class SpreadsheetService(private val drive: Drive) {
    fun list(): Flowable<SpreadsheetList> {
        return Flowable.create({
            val files = drive
                    .files()
                    .list()
                    .setQ("mimeType = 'application/vnd.google-apps.spreadsheet' " +
                            "and fullText contains 'Monthly Budget'")
                    .setFields("files(id, name, modifiedTime)")
                    .execute()
                    .files
            if (files != null) {
                val spreadsheets = files.map {
                    Spreadsheet(it.id, it.name, it.modifiedTime.value)
                }

                it.onNext(SpreadsheetList(spreadsheets))
            }

            it.onComplete()
        }, BackpressureStrategy.BUFFER)
    }
}