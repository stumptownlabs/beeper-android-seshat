package com.beeper.android_seshat.event

class NativeSerializedEventsResult{
    val array = mutableListOf<String>()
    fun add(serializedEvent:String){
        array.add(serializedEvent)
    }
}