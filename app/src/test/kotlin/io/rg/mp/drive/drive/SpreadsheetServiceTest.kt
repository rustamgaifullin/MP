package io.rg.mp.drive.drive

import com.google.api.client.testing.http.MockLowLevelHttpResponse
import io.reactivex.subscribers.TestSubscriber
import io.rg.mp.drive.SpreadsheetService
import io.rg.mp.drive.SubscribableTest
import io.rg.mp.drive.data.SpreadsheetList
import io.rg.mp.dummyFileSearch
import io.rg.mp.emptyFileSearch
import io.rg.mp.mockDriveClient
import io.rg.mp.mockResponse
import io.rg.mp.persistence.entity.Spreadsheet
import org.junit.Test
import java.util.LinkedList

class SpreadsheetServiceTest: SubscribableTest<SpreadsheetList>() {

    @Test
    fun `should return list of spreadsheets`() {
        //given
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(dummyFileSearch()))
        val sut = SpreadsheetService(mockDriveClient(responses))

        //when
        sut.list().subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertValue(SpreadsheetList(
                listOf(
                        Spreadsheet("id0", "name0", 1518185610345),
                        Spreadsheet("id1", "name1", 1518185610345),
                        Spreadsheet("id2", "name2", 1518185610345)
                )))
        testSubscriber.assertComplete()
    }

    @Test
    fun `should complete stream when no values retrieved`() {
        //given
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(emptyFileSearch()))
        val sut = SpreadsheetService(mockDriveClient(responses))

        //when
        sut.list().subscribe(testSubscriber)

        //then
        testSubscriber.assertNoErrors()
        testSubscriber.assertNoValues()
        testSubscriber.assertComplete()
    }

    @Test
    fun `should complete stream after deleting a spreadsheet`(){
        //given
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse("{}"))
        val sut = SpreadsheetService(mockDriveClient(responses))
        val testSubscriber = TestSubscriber<Any>()

        //when
        sut.deleteSpreadsheet("id").toFlowable<Any>().subscribe(testSubscriber)
        testSubscriber.assertNoErrors()
        testSubscriber.assertNoValues()
        testSubscriber.assertComplete()
    }
}