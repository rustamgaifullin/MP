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

fun dummyFileSearch() = """
                {
                "files": [
                {
                "id": "id0",
                "name": "name0",
                "modifiedTime": "2018-02-09T14:13:30.345Z"
                },
                {
                "id": "id1",
                "name": "name1",
                "modifiedTime": "2018-02-09T14:13:30.345Z"
                },
                {
                "id": "id2",
                "name": "name2",
                "modifiedTime": "2018-02-09T14:13:30.345Z"
                }
                ]
                }
            """.trimIndent()
fun emptyFileSearch() = """
                {
                    "files": []
                }
            """.trimIndent()
