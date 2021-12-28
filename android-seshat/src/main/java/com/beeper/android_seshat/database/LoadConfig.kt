package com.beeper.android_seshat.database

import com.beeper.android_seshat.event.Direction


class LoadConfig(
    val roomId: String,
    val limit: Int = 10,
    val fromEvent: String? = null,
    val direction: Direction = Direction.Forwards,
)