package io.rg.mp

fun oneFolder(name: String, id: String) = """
                {
                 "kind": "drive#fileList",
                 "incompleteSearch": false,
                 "files": [
                  {
                   "kind": "drive#file",
                   "id": "$id",
                   "name": "$name",
                   "mimeType": "application/vnd.google-apps.folder"
                  }
                 ]
                }
            """.trimIndent()

fun emptyFolder() = """
                {
                 "kind": "drive#fileList",
                 "incompleteSearch": false,
                 "files": []
                }
            """.trimIndent()

fun createFolder(id: String) = """
                {
                 "id": "$id"
                }
            """.trimIndent()

