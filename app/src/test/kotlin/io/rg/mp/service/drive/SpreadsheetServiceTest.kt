package io.rg.mp.service.drive

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.subscribers.TestSubscriber
import io.rg.mp.persistence.entity.Spreadsheet
import io.rg.mp.service.data.SpreadsheetList
import org.junit.Before
import org.junit.Test

class SpreadsheetServiceTest {

    private val drive: Drive = mock ()
    private val files: Drive.Files = mock ()
    private val list: Drive.Files.List = mock()
    private val fileList: FileList = mock()

    @Before
    fun setup() {
        whenever(drive.files()).thenReturn(files)
        whenever(files.list()).thenReturn(list)
        whenever(list.setQ(any())).thenReturn(list)
        whenever(list.execute()).thenReturn(fileList)
    }

    @Test
    fun should_return_list_of_spreadsheets() {
        //given
        val sut = SpreadsheetService(drive)
        val testSubscriber = TestSubscriber<SpreadsheetList>()

        //when
        val file: File = mock {
            on { id }.thenReturn("0")
            on { name }.thenReturn("name")
        }
        whenever(fileList.files).thenReturn(listOf(file))
        sut.list().subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(SpreadsheetList(listOf(Spreadsheet("0", "name"))))
        testSubscriber.assertComplete()
    }

}