package io.rg.mp.service.sheet

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.rg.mp.persistence.entity.Category
import io.rg.mp.service.SubscribableTest
import io.rg.mp.service.data.CategoryList
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CategoryRetrieverServiceTest: SubscribableTest<CategoryList>() {

    private val sheetsService: Sheets = mock()
    private val spreadsheets: Sheets.Spreadsheets = mock()
    private val values: Sheets.Spreadsheets.Values = mock()

    private lateinit var sut: CategoryRetrieverService

    @Before
    fun setup() {
        whenever(sheetsService.spreadsheets()).thenReturn(spreadsheets)
        whenever(spreadsheets.values()).thenReturn(values)

        sut = CategoryRetrieverService(sheetsService)
    }

    @Test
    fun `should successfully return categories`() {
        //given
        val listOfCategories: List<List<Any>> = listOf(listOf("category"))

        //when
        setToResponse(listOfCategories)
        sut.all("").subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { (list) -> list.contains(Category("category", "")) }
        testSubscriber.assertComplete()
    }

    @Test
    fun `should return empty list when no values retrieved`() {
        //when
        setToResponse(emptyList())
        sut.all("").subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { (list) -> list.isEmpty() }
        testSubscriber.assertComplete()
    }

    @Test
    fun `should return empty list when null occurs`() {
        //when
        setToResponse(null)
        sut.all("").subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { (list) -> list.isEmpty() }
        testSubscriber.assertComplete()
    }

    private fun setToResponse(listOfCategories: List<List<Any>>?) {
        val valueRange: ValueRange = mock {
            on { getValues() }.then { listOfCategories }
        }
        val request: Sheets.Spreadsheets.Values.Get = mock {
            on { execute() }.then { valueRange }
        }
        whenever(values.get(any(), any())).thenReturn(request)
    }

    @Test
    fun `should successfully save new category`() {
        //given
        val sut = CategoryRetrieverService(sheetsService)

        //when
        val result = sut.save(Category("", ""))

        //then
        Assert.assertEquals(result, true)
    }
}