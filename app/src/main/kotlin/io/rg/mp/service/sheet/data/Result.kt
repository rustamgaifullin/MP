package io.rg.mp.service.sheet.data

sealed class Result
class Saved : Result()
class NotSaved : Result()