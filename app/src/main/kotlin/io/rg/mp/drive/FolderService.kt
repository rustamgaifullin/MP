package io.rg.mp.drive

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import io.reactivex.Completable
import io.reactivex.Single
import io.rg.mp.drive.data.CreationResult
import java.util.*

class FolderService(private val drive: Drive) {
    companion object {
        private const val EMPTY_NAME = ""
        private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
    }

    fun rename(id: String, newName: String): Completable {
        return Completable.fromAction {
            val fileMetadata = File()
            fileMetadata.name = newName

            drive.files().update(id, fileMetadata).execute()
        }
    }

    fun copy(fromId: String, name: String = EMPTY_NAME): Single<CreationResult> {
        return Single.fromCallable {
            val fileMetadata = File()
            fileMetadata.name = name
            fileMetadata.parents = listOf(folderIdForCurrentYear())
            val copiedFile = drive.files()
                    .copy(fromId, fileMetadata)
                    .execute()
            CreationResult(copiedFile.id)
        }
    }

    private fun folderIdForCurrentYear(): String {
        val year = Calendar.getInstance().get(Calendar.YEAR).toString()
        val fileList = findFolder(year).files

        return if (fileList != null && fileList.size > 0)
            fileList.first().id
        else
            initializeFolders("Budget", year)
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

        return createFolder(name = yearFolder, parent = budgetId)
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