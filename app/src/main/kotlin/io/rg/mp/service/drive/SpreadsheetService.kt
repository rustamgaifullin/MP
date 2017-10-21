package io.rg.mp.service.drive

import com.google.api.services.drive.Drive
import io.reactivex.Flowable
import io.rg.mp.persistence.entity.Spreadsheet
import io.rg.mp.service.data.SpreadsheetList

class SpreadsheetService (private val drive: Drive) {

    fun list(): Flowable<SpreadsheetList> {
        return Flowable.fromCallable {
            val files = drive
                    .files()
                    .list()
                    .setQ("mimeType = 'application/vnd.google-apps.spreadsheet'")
                    .execute()
                    .files
            val spreadsheets = files.map {
                Spreadsheet(it.id, it.name)
            }

            SpreadsheetList(spreadsheets)
        }
    }
}