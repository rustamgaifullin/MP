package io.rg.mp.drive.data

sealed class Result
class Saved : Result()
class NotSaved : Result()