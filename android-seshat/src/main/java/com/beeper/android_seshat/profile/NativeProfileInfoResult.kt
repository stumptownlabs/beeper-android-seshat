package com.beeper.android_seshat.profile

class NativeProfileInfoResult{
    val info = mutableMapOf<String,Long>()
    fun add(mxId: String, profilePtr:Long){
        info[mxId] = profilePtr
    }
}