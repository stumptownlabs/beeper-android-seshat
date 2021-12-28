package com.beeper.android_seshat.event

enum class EventType(val code : Int){
    Message(0),
    Name(1),
    Topic(2);

    companion object {
        private val types = values().associateBy { it.code }
        fun fromCode(value: Int) = types[value] ?: Message
    }
}