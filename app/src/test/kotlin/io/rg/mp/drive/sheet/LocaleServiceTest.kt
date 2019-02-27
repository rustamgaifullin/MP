package io.rg.mp.drive.sheet

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.rg.mp.drive.LocaleService
import org.junit.Before
import org.junit.Test

class LocaleServiceTest {

    private val sheetsService: Sheets = mock()
    private val spreadsheets: Sheets.Spreadsheets = mock()
    private val get: Sheets.Spreadsheets.Get = mock()
    private val response: Spreadsheet = mock()

    @Before
    fun setup() {
        whenever(sheetsService.spreadsheets()).thenReturn(spreadsheets)
        whenever(spreadsheets.get(any())).thenReturn(get)
        whenever(get.setIncludeGridData(any())).thenReturn(get)
        whenever(get.setRanges(any())).thenReturn(get)
        whenever(get.setFields(any())).thenReturn(get)
        whenever(get.execute()).thenReturn(response)
    }

    @Test
    fun `should return locale from google services`() {
        val sut = LocaleService(sheetsService)
        val properties: SpreadsheetProperties = mock()

        whenever(response.properties).thenReturn(properties)
        whenever(properties.locale).thenReturn("en_GB")

        sut.getBy("id").test()
                .assertNoErrors()
                .assertValue { it == "en_GB" }
                .assertComplete()
                .dispose()
    }

    @Test
    fun `should complete observer when response from google service is null`() {
        val sut = LocaleService(sheetsService)
        val properties: SpreadsheetProperties = mock()

        whenever(response.properties).thenReturn(properties)
        whenever(properties.locale).thenReturn(null)

        sut.getBy("id").test()
                .assertNoErrors()
                .assertNoValues()
                .assertComplete()
                .dispose()
    }@Test
    fun `should complete observer when response from google service is empty`() {
        val sut = LocaleService(sheetsService)
        val properties: SpreadsheetProperties = mock()

        whenever(response.properties).thenReturn(properties)
        whenever(properties.locale).thenReturn("")

        sut.getBy("id").test()
                .assertNoErrors()
                .assertNoValues()
                .assertComplete()
                .dispose()
    }
}