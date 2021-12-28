package com.beeper.android_seshat.search

class NativeSearchResults{
    val array = mutableListOf<Long>()
    fun add(value:Long){
        array.add(value)
    }
}