package com.beeper.android_seshat.event

import com.beeper.android_seshat.profile.Profile

class NativeEventList(events: Map<Event, Profile>){
    private val eventList = mutableListOf<Long>()
    private val profileList = mutableListOf<Long>()

    init{
        events.onEach {
            eventList.add(it.key.ptr)
            profileList.add(it.value.ptr)
        }
    }
    private val eventsPointers : Map<Long,Long> = events.map {
        it.key.ptr to it.value.ptr
    }.toMap()

    fun getEvents(): LongArray {
        return eventList.toLongArray()
    }

    fun getProfiles(): LongArray {
        return profileList.toLongArray()
    }

}