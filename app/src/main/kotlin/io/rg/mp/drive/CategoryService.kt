package io.rg.mp.drive

import com.google.api.services.sheets.v4.Sheets
import io.reactivex.Flowable
import io.rg.mp.drive.data.CategoryList
import io.rg.mp.persistence.entity.Category

class CategoryService(private val googleSheetService: Sheets) {
    fun getListBy(sheetId: String): Flowable<CategoryList> {
        return Flowable.fromCallable {
            val range = "B29:\$F"
            val response = googleSheetService
                    .spreadsheets()
                    .values()
                    .get(sheetId, range)
                    .execute()

            var categoryList = emptyList<Category>()

            if (response.getValues() != null) {
                categoryList = response.getValues()
                        .filter { it.size > 0 }
                        .map {
                            Category(
                                    it[0].toString(),
                                    it[1].toString(),
                                    it[2].toString(),
                                    it[3].toString(),
                                    sheetId)
                        }
            }

            CategoryList(categoryList)
        }
    }
}