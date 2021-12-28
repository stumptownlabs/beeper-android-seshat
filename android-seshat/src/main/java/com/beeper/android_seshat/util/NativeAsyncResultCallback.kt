package com.beeper.android_seshat.util

interface NativeAsyncResultCallback<T>{
    fun onResult(value: T)
    fun onError(errorCode: Int, errorMessage: String = String())
}