package io.rg.mp.drive.sheet

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.rg.mp.persistence.entity.Category
import io.rg.mp.drive.CategoryService
import io.rg.mp.drive.SubscribableTest
import io.rg.mp.drive.data.CategoryList
import org.junit.Before
import org.junit.Test

class CategoryServiceTest : SubscribableTest<CategoryList>() {

    private val sheetsService: Sheets = mock()
    private val spreadsheets: Sheets.Spreadsheets = mock()
    private val values: Sheets.Spreadsheets.Values = mock()

    private lateinit var sut: CategoryService

    @Before
    fun setup() {
        whenever(sheetsService.spreadsheets()).thenReturn(spreadsheets)
        whenever(spreadsheets.values()).thenReturn(values)

        sut = CategoryService(sheetsService)
    }

    @Test
    fun `should successfully return categories`() {
        //given
        val listOfCategories: List<List<Any>> = listOf(listOf("category"))

        //when
        setToResponse(listOfCategories)
        sut.getListBy("").subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { (list) -> list.contains(Category("category", "")) }
        testSubscriber.assertComplete()
    }

    @Test
    fun `should return empty list when no values retrieved`() {
        //when
        setToResponse(emptyList())
        sut.getListBy("").subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { (list) -> list.isEmpty() }
        testSubscriber.assertComplete()
    }

    @Test
    fun `should return empty list when one row with no cells retrieved`() {
        //when
        setToResponse(listOf(emptyList()))
        sut.getListBy("").subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { (list) -> list.isEmpty() }
        testSubscriber.assertComplete()
    }

    @Test
    fun `should return empty list when null occurs`() {
        //when
        setToResponse(null)
        sut.getListBy("").subscribe(testSubscriber)

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
}