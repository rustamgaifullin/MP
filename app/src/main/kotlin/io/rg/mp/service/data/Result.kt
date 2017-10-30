package io.rg.mp.service.data

sealed class Result
class Saved : Result()
class NotSaved : Result()