package io.rg.mp

fun balance() = """
                    {
                      "valueRanges": [
                        {
                          "values": [
                            [
                              "2000"
                            ]
                          ]
                        },
                        {
                          "values": [
                            [
                              "1000"
                            ]
                          ]
                        },
                        {
                          "values": [
                            [
                              "200"
                            ]
                          ]
                        }
                      ]
                    }
                    """.trimIndent()

fun emptyBalance() = """
                    {
                        "valueRanges": [
                        {},
                        {},
                        {}
                        ]
                    }
                    """.trimIndent()

fun partialBalance() = """
                    {
                        "valueRanges": [
                        {},
                        {},
                        {
                          "values": [
                            [
                              "200"
                            ]
                          ]
                        }
                        ]
                    }
                    """.trimIndent()

fun updatePlannedValue() = """
                    {
                        "spreadsheetId": "id",
                        "updatedRange": "Summary!D30",
                        "updatedRows": 1,
                        "updatedColumns": 1,
                        "updatedCells": 1
                    }
                    """.trimIndent()