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