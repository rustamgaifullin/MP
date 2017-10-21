package io.rg.mp.service.sheet

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.subscribers.TestSubscriber
import io.rg.mp.persistence.entity.Category
import io.rg.mp.service.data.CategoryList
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CategoryRetrieverServiceTest {

    private val sheetsService: Sheets = mock()
    private val spreadsheets: Sheets.Spreadsheets = mock()
    private val values: Sheets.Spreadsheets.Values = mock()

    @Before
    fun setup() {
        whenever(sheetsService.spreadsheets()).thenReturn(spreadsheets)
        whenever(spreadsheets.values()).thenReturn(values)
    }

    @Test
    fun should_successfully_return_categories() {
        //given
        val sut = CategoryRetrieverService(sheetsService)
        val listOfCategories: List<List<Any>> = listOf(listOf("category"))
        val testSubscriber = TestSubscriber<CategoryList>()

        //when
        val valueRange: ValueRange = mock {
            on { getValues() }.then { listOfCategories }
        }
        val request: Sheets.Spreadsheets.Values.Get = mock {
            on { execute() }.then { valueRange }
        }
        whenever(values.get(any(), any())).thenReturn(request)

        sut.all("").subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { (list) -> list.contains(Category("category", "")) }
        testSubscriber.assertComplete()
    }

    @Test
    fun should_successfully_save_new_category() {
        //given
        val sut = CategoryRetrieverService(sheetsService)

        //when
        val result = sut.save(Category("", ""))

        //then
        Assert.assertEquals(result, true)
    }
}