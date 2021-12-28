package com.beeper.android_seshat.event

import com.beeper.android_seshat.profile.Profile
import java.lang.Exception

class NativeFileEventList(){
    private val serializedEventList = mutableListOf<String>()
    private val profileList = mutableListOf<Long>()

    fun add(serializedEvent: String, profilePtr: Long){
        serializedEventList.add(serializedEvent)
        profileList.add(profilePtr)
    }

    fun getResult() : List<Pair<String,Profile>>{
        if(serializedEventList.size != profileList.size)
            throw Exception("Invalid event/profile list sizes")

        val result = mutableListOf<Pair<String,Profile>>()
        serializedEventList.onEachIndexed { index, serializedEvent ->
            result.add(Pair(serializedEvent,Profile(profileList[index])))
        }
        return result.toList()
    }
}