package io.rg.mp.drive.sheet

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AppendValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.rg.mp.drive.TransactionService
import io.rg.mp.drive.data.Expense
import io.rg.mp.drive.data.Saved
import io.rg.mp.persistence.entity.Category
import io.rg.mp.persistence.entity.Transaction
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

class TransactionServiceTest {

    private val sheetsService: Sheets = mock()
    private val spreadsheets: Sheets.Spreadsheets = mock()
    private val values: Sheets.Spreadsheets.Values = mock()
    private val append: Sheets.Spreadsheets.Values.Append = mock()
    private val get: Sheets.Spreadsheets.Values.Get = mock()

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
        sut.saveExpense(expense, "").test()
                .assertNoErrors()
                .assertValue { result -> result is Saved }
                .assertComplete()
                .dispose()
    }

    @Test
    fun `should clear transactions`() {
        val spreadsheetId = "id"
        val batchClear: Sheets.Spreadsheets.Values.BatchClear = mock()

        whenever(values.batchClear(eq(spreadsheetId), any())).thenReturn(batchClear)

        sut.clearAllTransactions(spreadsheetId).test()
                .assertNoErrors()
                .assertComplete()
                .dispose()
    }

    @Test
    fun `should successfully retrieve all transactions`() {
        val spreadsheetId = "id"
        val valueRange: ValueRange = mock {
            on { getValues() }.then { listOf(listOf("date", "amount", "desc", "category")) }
        }

        whenever(values.get(eq(spreadsheetId), anyString())).thenReturn(get)
        whenever(get.execute()).thenReturn(valueRange)

        sut.all(spreadsheetId).test()
                .assertNoErrors()
                .assertValue {
                    it.list.size == 1
                            && it.list[0] == Transaction(
                            0L,
                            "date",
                            "amount",
                            "desc",
                            "category",
                            spreadsheetId)
                }
                .dispose()
    }
}