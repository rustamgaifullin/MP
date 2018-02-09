package io.rg.mp.drive

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import io.reactivex.Completable
import io.reactivex.Single
import java.util.Calendar

class FolderService(private val drive: Drive) {
    companion object {
        const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
    }

    fun moveToFolder(spreadsheetId: String, toFolder: String): Completable {
        return Completable.fromAction {
            val content = File()

            drive.files().update(spreadsheetId, content)
                    .setAddParents(toFolder)
                    .execute()
        }
    }

    fun folderIdForCurrentYear(): Single<String> {
        return Single.fromCallable {
            val year = Calendar.getInstance().get(Calendar.YEAR).toString()
            val fileList = findFolder(year).files

            if (fileList != null && fileList.size > 0)
                fileList.first().id else
                initializeFolders("Budget", year)
        }
    }

    private fun findFolder(name: String): FileList {
        return drive.files()
                .list()
                .setQ("name = '$name' and mimeType = '$FOLDER_MIME_TYPE'")
                .setFields("files/id")
                .execute()
    }

    private fun initializeFolders(rootFolder: String, yearFolder: String): String {
        val budgetFileList = findFolder(rootFolder).files

        val budgetId = if (budgetFileList.size > 0)
            budgetFileList.first().id else
            createFolder(rootFolder)

        return createFolder(name = yearFolder, parent =  budgetId)
    }

    private fun createFolder(name: String, parent: String = ""): String {
        val content = File()
        content.name = name
        content.mimeType = FOLDER_MIME_TYPE
        content.parents = listOf(parent)

        return drive.files()
                .create(content)
                .setFields("id")
                .execute()
                .id
    }
}