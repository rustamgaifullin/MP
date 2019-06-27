package io.rg.mp.drive.sheet

import com.google.api.client.testing.http.MockLowLevelHttpResponse
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.rg.mp.drive.CategoryService
import io.rg.mp.mockResponse
import io.rg.mp.mockSheetClient
import io.rg.mp.persistence.entity.Category
import io.rg.mp.updatePlannedValue
import org.junit.Before
import org.junit.Test
import java.util.LinkedList

class CategoryServiceTest {
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
        val listOfCategories: List<List<Any>> = listOf(listOf("category", "", "", "", ""))

        //when
        setToResponse(listOfCategories)
        sut.getListBy("").test()
                .assertNoErrors()
                .assertValue {
                    (list) -> list.contains(Category("category", "", "", "", 29, ""))
                }
                .assertComplete()
    }

    @Test
    fun `should return empty list when no values retrieved`() {
        //when
        setToResponse(emptyList())
        sut.getListBy("").test()
                .assertNoErrors()
                .assertValue { (list) -> list.isEmpty() }
                .assertComplete()
    }

    @Test
    fun `should return empty list when one row with no cells retrieved`() {
        //when
        setToResponse(listOf(emptyList()))
        sut.getListBy("")
                .test()
                .assertNoErrors()
                .assertValue { (list) -> list.isEmpty() }
                .assertComplete()
    }

    @Test
    fun `should return empty list when null occurs`() {
        //when
        setToResponse(null)
        sut.getListBy("")
                .test()
                .assertNoErrors()
                .assertValue { (list) -> list.isEmpty() }
                .assertComplete()
    }

    @Test
    fun `should successfully update planned value`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(updatePlannedValue()))

        val sut = CategoryService(mockSheetClient(responses))

        sut.updateCategory(Category("", "", "", "", 0, ""))
                .test()
                .assertNoErrors()
                .assertComplete()

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