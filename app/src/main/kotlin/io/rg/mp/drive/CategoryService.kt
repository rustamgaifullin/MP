package io.rg.mp.drive

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import io.reactivex.Completable
import io.reactivex.Completable.fromCallable
import io.reactivex.Flowable
import io.rg.mp.drive.data.CategoryList
import io.rg.mp.drive.data.NotSaved
import io.rg.mp.drive.data.Result
import io.rg.mp.drive.data.Saved
import io.rg.mp.persistence.entity.Category
import io.rg.mp.utils.extractDouble

class CategoryService(private val googleSheetService: Sheets) {
    companion object {
        private const val STARTED_ROW = 29
        private const val RANGE = "B$STARTED_ROW:\$F"
        private const val PLANNED_CELL = "D"
        private const val CATEGORY_RANGE = "B29:F29"
    }

    fun getListBy(sheetId: String): Flowable<CategoryList> {
        return Flowable.fromCallable {
            val response = googleSheetService
                    .spreadsheets()
                    .values()
                    .get(sheetId, RANGE)
                    .execute()

            var categoryList = emptyList<Category>()

            if (response.getValues() != null) {
                categoryList = response.getValues()
                        .filter { it.size > 0 }
                        .mapIndexed { index, row ->
                            Category(
                                    row[0].toString(),
                                    row[2].toString(),
                                    row[3].toString(),
                                    row[4].toString(),
                                    STARTED_ROW + index,
                                    sheetId)
                        }
            }

            CategoryList(categoryList)
        }
    }

    fun updateCategory(category: Category): Completable {
        return fromCallable {
            val range = "$PLANNED_CELL${category.rowNumber}"
            val value = ValueRange()
            value.setValues(listOf(listOf(category.planned.extractDouble())))

            googleSheetService
                    .spreadsheets()
                    .values()
                    .update(category.spreadsheetId, range, value)
                    .setValueInputOption("RAW")
                    .execute()
        }
    }

    //TODO:Need to think how to insert formula for last two cells
    fun createCategory(spreadsheetId: String, name: String, amount: String): Flowable<Result> {
        return Flowable.fromCallable {
            val body = listOf(name, "", amount, "", "")

            val result = googleSheetService
                    .spreadsheets()
                    .values()
                    .append(spreadsheetId, CATEGORY_RANGE, ValueRange().setValues(listOf(body)))
                    .setValueInputOption("USER_ENTERED")
                    .execute()

            if (result.tableRange.isNotEmpty()) {
                Saved(spreadsheetId)
            } else {
                NotSaved()
            }
        }
    }
}