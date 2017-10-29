package io.rg.mp.service.sheet

import com.google.api.services.sheets.v4.Sheets
import io.reactivex.Flowable
import io.rg.mp.persistence.entity.Category
import io.rg.mp.service.data.CategoryList

class CategoryRetrieverService (private val googleSheetService: Sheets) {
    fun all(sheetId: String): Flowable<CategoryList> {
        return Flowable.fromCallable {
            val range = "B29:\$B"
            val response = googleSheetService
                    .spreadsheets()
                    .values()
                    .get(sheetId, range)
                    .execute()

            var categoryList = emptyList<Category>()

            if (response.getValues() != null) {
                categoryList = response.getValues().map {
                    Category(it[0].toString(), sheetId)
                }
            }

            CategoryList(categoryList)
        }
    }

    fun save(data: Category): Boolean {
        return true
    }
}