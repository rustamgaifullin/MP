package io.rg.mp.service.sheet

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.rg.mp.persistence.entity.Category
import io.rg.mp.service.SubscribableTest
import io.rg.mp.service.data.Expense
import io.rg.mp.service.data.Result
import io.rg.mp.service.data.Saved
import org.junit.Before
import org.junit.Test
import java.util.*

class ExpenseServiceTest: SubscribableTest<Result>() {

    private val sheetsService: Sheets = mock()
    private val spreadsheets: Sheets.Spreadsheets = mock()
    private val values: Sheets.Spreadsheets.Values = mock()
    private val append: Sheets.Spreadsheets.Values.Append = mock()

    private lateinit var sut: ExpenseService

    @Before
    fun setup() {
        whenever(sheetsService.spreadsheets()).thenReturn(spreadsheets)
        whenever(spreadsheets.values()).thenReturn(values)

        sut = ExpenseService(sheetsService)
    }

    @Test
    fun `should successfully save expense item`() {
        //given
        val expense = Expense(Date(), 5.5f, "", Category("", ""))
        val appendReusult: AppendValuesResponse = mock()

        //when
        whenever(values.append(any(), any(), any())).thenReturn(append)
        whenever(append.execute()).thenReturn(appendReusult)
        whenever(appendReusult.tableRange).thenReturn("A1:A2")
        sut.save(expense, "").subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { result ->  result is Saved }
        testSubscriber.assertComplete()
    }
}