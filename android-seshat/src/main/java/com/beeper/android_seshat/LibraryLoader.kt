package com.beeper.android_seshat


object LibraryLoader {
    private var isNativeLibLoaded = false
    @Synchronized
    internal fun ensureNativeLibIsLoaded(){
        if (!isNativeLibLoaded) {
            System.loadLibrary("android_seshat_bindings")
            isNativeLibLoaded = true
        }
    }

}

