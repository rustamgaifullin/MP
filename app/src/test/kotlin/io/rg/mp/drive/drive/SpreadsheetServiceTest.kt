package io.rg.mp.drive.drive

import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.rg.mp.persistence.entity.Spreadsheet
import io.rg.mp.drive.SpreadsheetList
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.drive.SubscribableTest
import org.junit.Before
import org.junit.Test

class SpreadsheetServiceTest: SubscribableTest<SpreadsheetList>() {

    private val drive: Drive = mock ()
    private val files: Drive.Files = mock ()
    private val list: Drive.Files.List = mock()
    private val fileList: FileList = mock()

    private lateinit var sut: SpreadsheetService

    @Before
    fun setup() {
        whenever(drive.files()).thenReturn(files)
        whenever(files.list()).thenReturn(list)
        whenever(list.setQ(any())).thenReturn(list)
        whenever(list.setFields(any())).thenReturn(list)
        whenever(list.execute()).thenReturn(fileList)

        sut = SpreadsheetService(drive)
    }

    @Test
    fun `should return list of spreadsheets`() {
        //given
        val file: File = mock {
            on { id }.thenReturn("0")
            on { name }.thenReturn("name")
            on { modifiedTime }.thenReturn(DateTime(123))
        }

        //when
        whenever(fileList.files).thenReturn(listOf(file))
        sut.list().subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(SpreadsheetList(listOf(Spreadsheet("0", "name", 123))))
        testSubscriber.assertComplete()
    }

    @Test
    fun `should complete stream when no values retrieved`() {
        //when
        whenever(fileList.files).thenReturn(null)
        sut.list().subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertNoValues()
        testSubscriber.assertComplete()
    }

}