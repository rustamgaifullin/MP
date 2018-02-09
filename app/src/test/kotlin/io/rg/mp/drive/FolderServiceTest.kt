package io.rg.mp.drive

import com.google.api.client.testing.http.MockLowLevelHttpResponse
import io.rg.mp.createFolder
import io.rg.mp.emptyFolder
import io.rg.mp.mockDriveClient
import io.rg.mp.mockResponse
import io.rg.mp.oneFolder
import org.junit.Test
import java.util.LinkedList


class FolderServiceTest : SubscribableTest<String>() {

    @Test
    fun `should find folder for current year`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(oneFolder("2018", "yearFolderId")))

        val sut = FolderService(mockDriveClient(responses))

        sut.folderIdForCurrentYear().toFlowable().subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
                .assertValue {
                    it == "yearFolderId"
                }
    }

    @Test
    fun `should create folder for current year if it's not exist`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(emptyFolder()))
        responses.add(mockResponse(oneFolder("Budget", "budgetId")))
        responses.add(mockResponse(createFolder("newIdForYearFolder")))

        val sut = FolderService(mockDriveClient(responses))

        sut.folderIdForCurrentYear().toFlowable().subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
                .assertValue {
                    it == "newIdForYearFolder"
                }
    }

    @Test
    fun `should initialize all folders if they are not exist`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(emptyFolder()))
        responses.add(mockResponse(emptyFolder()))
        responses.add(mockResponse(createFolder("budgetId")))
        responses.add(mockResponse(createFolder("newIdForYearFolder")))

        val sut = FolderService(mockDriveClient(responses))

        sut.folderIdForCurrentYear().toFlowable().subscribe(testSubscriber)

        testSubscriber.assertNoErrors()
                .assertValue {
                    it == "newIdForYearFolder"
                }
    }
}