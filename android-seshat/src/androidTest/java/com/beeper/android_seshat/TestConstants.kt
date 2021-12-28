package com.beeper.android_seshat

import com.beeper.android_seshat.event.Event
import com.beeper.android_seshat.event.EventType
import com.beeper.android_seshat.profile.Profile

const val defaultEventId = "$15163622445EBvZJ:localhost"
const val EVENT_SOURCE = "{" +
        "\"content\": {" +
        "\"body\": \"Test message\", \"msgtype\": \"m.text\"" +
        "}," +
        "\"room_id\": \"!TESTROOM\"," +
        "\"event_id\": \"${defaultEventId}\"," +
        "\"origin_server_ts\": 1516362244026," +
        "\"sender\": \"@example2:localhost\"," +
        "\"type\": \"m.room.message\"," +
        "\"unsigned\": {\"age\": 43289803095}," +
        "\"user_id\": \"@example2:localhost\"," +
        "\"age\": 43289803095" +
        "}"

const val IMAGE_SOURCE = "{" +
        "content: {" +
        "body: Test image, msgtype: m.image" +
        "}," +
        "event_id: $15163622445EBvZJlocalhost" +
        "origin_server_ts: 1516362244026," +
        "sender: @example2localhost," +
        "type: m.room.message," +
        "unsigned: {age: 43289803095}," +
        "user_id: @example2localhost," +
        "age: 43289803095" +
        "}"

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
    EVENT_SOURCE
)

val event2 = Event(
    EventType.Message,
    "Test message Bob",
    "m.text",
    defaultEventId + 1,
    "@example2:localhost",
    151636_2244025,
    "!test_room:localhost",
    EVENT_SOURCE
)

val event3 = Event(
    EventType.Message,
    "Test message Jim",
    "m.text",
    defaultEventId + 2,
    "@example2:localhost",
    151636_2244026,
    "!test_room:localhost",
    EVENT_SOURCE
)


val imageEvent =  Event(
EventType.Message,
"Test image",
  "m.image",
"$15163622471image:localhost",
"@example2:localhost",
151636_2244050,
"!test_room:localhost",
IMAGE_SOURCE,
)