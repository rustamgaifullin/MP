package io.rg.mp.drive

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import io.reactivex.Single
import io.rg.mp.drive.data.CreationResult

class CopyService(private val googleSheetService: Sheets) {
    companion object {
        private const val DEFAULT_TEMPLATE_ID = "1Ydrns7Pv4mf17D4eZjLD_sNL78LPH0SC05Lt_U45JFk"
        private const val EMPTY_NAME = ""
    }

    fun copy(name: String = EMPTY_NAME, fromId: String = DEFAULT_TEMPLATE_ID): Single<CreationResult> {
        return Single.fromCallable{
            val newFile = copyFrom(fromId, name)

            CreationResult(newFile.spreadsheetId)
        }
    }

    private fun copyFrom(id: String, name: String): Spreadsheet {
        val content = getFileFrom(id)
        content.spreadsheetId = null
        content.properties.title = name.ifBlank { "Copy of ${content.properties.title}" }

        return googleSheetService
                .spreadsheets()
                .create(content)
                .execute()
    }

    private fun getFileFrom(id: String): Spreadsheet {
        return googleSheetService.spreadsheets()
                .get(id)
                .setIncludeGridData(true)
                .execute()
    }
}