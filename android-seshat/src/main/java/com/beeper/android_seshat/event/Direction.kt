package com.beeper.android_seshat.event

enum class Direction(val code : Int){
    Forwards(0),
    Backwards(1);
    companion object {
        private val types = values().associateBy { it.code }
        fun fromCode(value: Int) = types[value] ?: Forwards
    }
}