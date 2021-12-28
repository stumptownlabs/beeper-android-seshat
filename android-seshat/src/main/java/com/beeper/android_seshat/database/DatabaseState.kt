package com.beeper.android_seshat.database

sealed class DatabaseState
object Open : DatabaseState()
object Closed : DatabaseState()