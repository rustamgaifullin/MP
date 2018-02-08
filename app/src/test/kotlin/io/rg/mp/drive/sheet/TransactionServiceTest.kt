package io.rg.mp.drive.sheet

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.rg.mp.persistence.entity.Category
import io.rg.mp.drive.TransactionService
import io.rg.mp.drive.SubscribableTest
import io.rg.mp.drive.data.Expense
import io.rg.mp.drive.data.Result
import io.rg.mp.drive.data.Saved
import org.junit.Before
import org.junit.Test

class TransactionServiceTest : SubscribableTest<Result>() {

    private val sheetsService: Sheets = mock()
    private val spreadsheets: Sheets.Spreadsheets = mock()
    private val values: Sheets.Spreadsheets.Values = mock()
    private val append: Sheets.Spreadsheets.Values.Append = mock()

    private lateinit var sut: TransactionService

    @Before
    fun setup() {
        whenever(sheetsService.spreadsheets()).thenReturn(spreadsheets)
        whenever(spreadsheets.values()).thenReturn(values)

        sut = TransactionService(sheetsService)
    }

    @Test
    fun `should successfully save expense item`() {
        //given
        val expense = Expense("01/01/17", 5.5f, "", Category("", ""))
        val appendResult: AppendValuesResponse = mock()

        //when
        whenever(values.append(any(), any(), any())).thenReturn(append)
        whenever(append.setValueInputOption(any())).thenReturn(append)
        whenever(append.execute()).thenReturn(appendResult)
        whenever(appendResult.tableRange).thenReturn("A1:A2")
        sut.saveExpense(expense, "").subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue { result -> result is Saved }
        testSubscriber.assertComplete()
    }
}