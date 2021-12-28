package com.beeper.android_seshat.database

sealed class NativeOption{
    abstract fun hasSome() : Boolean
}

class NativeSome(private val value: Long) : NativeOption(){
    override fun hasSome() : Boolean = true
    fun getValue() : Long = value
}
object NativeNone : NativeOption(){
    override fun hasSome(): Boolean = false
}