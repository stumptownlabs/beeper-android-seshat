package com.beeper.android_seshat

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import com.beeper.android_seshat.event.Event
import com.beeper.android_seshat.event.EventType


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class EventTest {
    @Test
    fun create_event()
    {
        Event(
            EventType.Message,
            "Test message",
            "m.text",
            "$15163622445EBvZJ:localhost",
            "@example2:localhost",
            151636_2244026,
            "!test_room:localhost",
            EVENT_SOURCE
        )
    }

    @Test
    fun get_event_fields()
    {
        val testType = EventType.Message
        val testContentValue = "Test message"
        val testMsgType = "m.text"
        val testEventId = "$15163622445EBvZJ:localhost"
        val testSender = "@example2:localhost"
        val testServerTs = 151636_2244026
        val testRoomId = "!test_room:localhost"

        val event = Event(
            testType,
            testContentValue,
            testMsgType,
            testEventId,
            testSender,
            testServerTs,
            testRoomId,
            EVENT_SOURCE
        )
        val type = event.getEventType()
        MatcherAssert.assertThat(type, CoreMatchers.equalTo(testType))
        val contentValue = event.getContentValue()
        MatcherAssert.assertThat(contentValue, CoreMatchers.equalTo(testContentValue))
        val msgType = event.getMessageType()
        MatcherAssert.assertThat(msgType, CoreMatchers.equalTo(testMsgType))
        val eventId = event.getEventId()
        MatcherAssert.assertThat(eventId, CoreMatchers.equalTo(testEventId))
        val sender = event.getSender()
        MatcherAssert.assertThat(sender, CoreMatchers.equalTo(testSender))
        val serverTs = event.getServerTs()
        MatcherAssert.assertThat(serverTs, CoreMatchers.equalTo(testServerTs))
        val roomId = event.getRoomId()
        MatcherAssert.assertThat(roomId, CoreMatchers.equalTo(testRoomId))

        val event2 = Event(
            testType,
            testContentValue,
            null,
            testEventId,
            testSender,
            testServerTs,
            testRoomId,
            EVENT_SOURCE
        )
        val nullMessageType = event2.getMessageType()
        MatcherAssert.assertThat(nullMessageType, CoreMatchers.equalTo(null))
    }

    @Test
    fun free_event() {
        val event = Event(
            EventType.Message,
            "Test message",
            "m.text",
            "$15163622445EBvZJ:localhost",
            "@example2:localhost",
            151636_2244026,
            "!test_room:localhost",
            EVENT_SOURCE
        )

        event.testFinalize()
    }

}
