package com.beeper.android_seshat

import com.beeper.android_seshat.event.Event
import com.beeper.android_seshat.event.EventType
import com.beeper.android_seshat.profile.Profile

const val defaultEventId = "$15163622445EBvZJ:localhost"

const val displayName = "Alice"
const val avatarURL = "https://test.com"
val profile = Profile(displayName, avatarURL)
val event = Event(
    EventType.Message,
    "Test message John",
    "m.text",
    defaultEventId,
    "@example2:localhost",
    151636_2244024,
    "!test_room:localhost",
)

val event2 = Event(
    EventType.Message,
    "Test message Bob",
    "m.text",
    defaultEventId + 1,
    "@example2:localhost",
    151636_2244025,
    "!test_room:localhost",
)

val event3 = Event(
    EventType.Message,
    "Test message Jim",
    "m.text",
    defaultEventId + 2,
    "@example2:localhost",
    151636_2244026,
    "!test_room:localhost",
)


val imageEvent =  Event(
EventType.Message,
"Test image",
  "m.image",
"$15163622471image:localhost",
"@example2:localhost",
151636_2244050,
"!test_room:localhost",
)