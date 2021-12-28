package com.beeper.android_seshat.util

sealed class Result<out T, out U>
data class Success<out T>(val value : T) : Result<T, Nothing>()
data class Error<out U>(val reason : U) : Result<Nothing, U>()