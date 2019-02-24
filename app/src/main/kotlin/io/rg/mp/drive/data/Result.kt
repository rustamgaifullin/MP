package io.rg.mp.drive.data

sealed class Result
class Saved(val spreadsheetId: String) : Result()
class NotSaved : Result()