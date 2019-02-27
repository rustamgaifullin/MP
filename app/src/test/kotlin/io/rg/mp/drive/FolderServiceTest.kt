package io.rg.mp.drive

import com.google.api.client.testing.http.MockLowLevelHttpResponse
import io.rg.mp.createFolder
import io.rg.mp.emptyFolder
import io.rg.mp.mockDriveClient
import io.rg.mp.mockResponse
import io.rg.mp.oneFolder
import org.junit.Test
import java.util.LinkedList


class FolderServiceTest {

    @Test
    fun `should find folder for current year`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(oneFolder("2018", "yearFolderId")))

        val sut = FolderService(mockDriveClient(responses))

        sut.folderIdForCurrentYear().toFlowable().test()
                .assertNoErrors()
                .assertValue {
                    it == "yearFolderId"
                }
                .dispose()
    }

    @Test
    fun `should create folder for current year if it's not exist`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(emptyFolder()))
        responses.add(mockResponse(oneFolder("Budget", "budgetId")))
        responses.add(mockResponse(createFolder("newIdForYearFolder")))

        val sut = FolderService(mockDriveClient(responses))

        sut.folderIdForCurrentYear().toFlowable().test()
                .assertNoErrors()
                .assertValue {
                    it == "newIdForYearFolder"
                }
                .dispose()
    }

    @Test
    fun `should initialize all folders if they are not exist`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(emptyFolder()))
        responses.add(mockResponse(emptyFolder()))
        responses.add(mockResponse(createFolder("budgetId")))
        responses.add(mockResponse(createFolder("newIdForYearFolder")))

        val sut = FolderService(mockDriveClient(responses))

        sut.folderIdForCurrentYear().toFlowable().test()
                .assertNoErrors()
                .assertValue {
                    it == "newIdForYearFolder"
                }
                .dispose()
    }

    @Test
    fun `should complete stream after moving a file to some folder`() {
        //given
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse("{}"))
        val sut = FolderService(mockDriveClient(responses))

        //when
        sut.moveToFolder("id", "folder").toFlowable<Any>().test()
                .assertNoErrors()
                .assertNoValues()
                .assertComplete()
                .dispose()
    }
}