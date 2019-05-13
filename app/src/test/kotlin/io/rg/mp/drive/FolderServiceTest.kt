package io.rg.mp.drive

import com.google.api.client.testing.http.MockLowLevelHttpResponse
import io.rg.mp.copyFile
import io.rg.mp.createFolder
import io.rg.mp.emptyFolder
import io.rg.mp.mockDriveClient
import io.rg.mp.mockResponse
import io.rg.mp.oneFolder
import io.rg.mp.renameFile
import org.junit.Test
import java.util.LinkedList


class FolderServiceTest {

    @Test
    fun `should copy file when folder for current year is existed`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(oneFolder("2018", "yearFolderId")))
        responses.add(mockResponse(copyFile()))

        val sut = FolderService(mockDriveClient(responses))

        sut.copy("name").test()
                .assertNoErrors()
                .assertValue {
                    it.id == "123456"
                }
    }

    @Test
    fun `should create folder for current year if it's not exist`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(emptyFolder()))
        responses.add(mockResponse(oneFolder("Budget", "budgetId")))
        responses.add(mockResponse(createFolder("newIdForYearFolder")))
        responses.add(mockResponse(copyFile()))

        val sut = FolderService(mockDriveClient(responses))

        sut.copy("name").test()
                .assertNoErrors()
                .assertValue {
                    it.id == "123456"
                }
    }

    @Test
    fun `should initialize all folders if they are not exist`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(emptyFolder()))
        responses.add(mockResponse(emptyFolder()))
        responses.add(mockResponse(createFolder("budgetId")))
        responses.add(mockResponse(createFolder("newIdForYearFolder")))
        responses.add(mockResponse(copyFile()))

        val sut = FolderService(mockDriveClient(responses))

        sut.copy("name").test()
                .assertNoErrors()
                .assertValue {
                    it.id == "123456"
                }
    }

    @Test
    fun `should rename a spreadsheet successfully`() {
        val responses = LinkedList<MockLowLevelHttpResponse>()
        responses.add(mockResponse(renameFile()))

        val sut = FolderService(mockDriveClient(responses))

        sut.rename("id", "newName").test()
                .assertNoErrors()
                .assertComplete()
    }
}