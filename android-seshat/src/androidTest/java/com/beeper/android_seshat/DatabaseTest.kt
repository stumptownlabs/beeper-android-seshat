package com.beeper.android_seshat

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.number.IsCloseTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.beeper.android_seshat.Utils.deleteFilesFromFolder
import com.beeper.android_seshat.database.*
import com.beeper.android_seshat.event.CrawlerCheckpoint
import com.beeper.android_seshat.event.Direction
import com.beeper.android_seshat.event.Event
import com.beeper.android_seshat.event.EventType
import com.beeper.android_seshat.search.SearchConfig
import com.beeper.android_seshat.util.Error
import com.beeper.android_seshat.util.Success
import org.hamcrest.CoreMatchers


/**
 * Instrumented test, which will execute on an Android device.
 */

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    @Before
    fun setUp() {
        //Always delete the database before starting a new test
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        deleteFilesFromFolder(appContext.filesDir.path)
    }

    @Test
    fun create_db() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val result = Database.get(appContext.filesDir.path)
        assertThat(result, instanceOf(Success::class.java))
    }


    @Test
    fun create_encrypted_db_with_config() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseConfig = DatabaseConfig(DatabaseLanguage.English, "testPassphrase")
        val result = Database.getWithConfig(appContext.filesDir.path, databaseConfig)
        if(result is Error){
            Log.w("create_encrypted_db_with_config error",
                "$ result ${result.reason.code} ${result.reason.message}" )
        }
        assertThat(result, instanceOf(Success::class.java))

        //Returning SQLCipherError while we don't bundle SQLCipher
    }

    @Test
    fun database_stats() {
        val initialDbSize = 100000L.toULong()
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val result = Database.get(appContext.filesDir.path) as Success
        val db = result.value

        val stats = db.getStats()
        val initialSize = stats.size
        assertThat(initialSize, greaterThanOrEqualTo(initialDbSize))
        assertThat(stats.roomCount, equalTo(0.toULong()))
        assertThat(stats.eventCount, equalTo(0.toULong()))

        db.addEvent(event, profile)
        db.addEvent(event2, profile)
        db.addEvent(event3, profile)

        db.forceCommit()
        db.reload()

        val statsAfter = db.getStats()
        val finalSize = statsAfter.size
        val insertedDataSize = 2483L.toULong()
        val deltaSize = finalSize - initialSize

        assertThat(finalSize, greaterThan(initialDbSize))
        assertThat(deltaSize, greaterThanOrEqualTo(insertedDataSize))
        assertThat(statsAfter.roomCount, equalTo(1.toULong()))
        assertThat(statsAfter.eventCount, equalTo(3.toULong()))
    }

    @Test
    fun db_delete() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        db.delete()
    }

    @Test
    fun db_shutdown() = runBlocking{
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        val result = db.shutdown()
        assertThat(result, instanceOf(Success::class.java))
    }

    @Test
    fun db_is_empty() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        assertThat((db.isEmpty() as Success).value, equalTo(true))

        db.addEvent(event, profile)
        db.forceCommit()
        db.reload()
        assertThat((db.isEmpty() as Success).value, equalTo(false))
    }

    @Test
    fun add_event_to_db() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        db.addEvent(event, profile)
    }

    @Test
    fun change_db_passphrase() {
        //val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        //val db = (Database.newInstance(appContext.filesDir.path) as Success).value
        //TODO: Enable change_db_passphrase tests
        //db.change_passphrase("test_passphrase")

    }

    @Test
    fun db_get_size() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        assertThat(db.getSize(), greaterThan(0L.toULong()))
    }

    @Test
    fun db_get_user_version() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        assertThat(db.getUserVersion(), equalTo(0L))
    }

    @Test
    fun db_set_user_version() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        db.setUserVersion(2L)
        assertThat(db.getUserVersion(), equalTo(2L))
    }

    @Test
    fun db_load_file_events() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        val loadConfig = LoadConfig(
            "!test_room:localhost",10, null,Direction.Forwards
        )
        db.addEvent(event,profile)
        db.addEvent(imageEvent,profile)
        db.commitSync()
        db.reload()
        val eventList = db.loadFileEvents(loadConfig)
        //Only 1 file event
        assertThat(eventList.size, equalTo(1))
        val eventList2 = db.loadFileEvents(LoadConfig(
            "!test_room:localhost",10, defaultEventId,Direction.Forwards))
        assertThat(eventList2.size, equalTo(1))
    }


    @Test
    fun db_delete_event() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value

        db.addEvent(event, profile)
        db.forceCommit()
        db.reload()
        assertThat(db.getStats().eventCount, equalTo(1.toULong()))

        val cantDeleteResult = db.deleteEvent("unexistent_event_id")
        assertThat(cantDeleteResult, instanceOf(Success::class.java))
        assertThat((cantDeleteResult as Success).value, equalTo(false))
        assertThat(db.getStats().eventCount, equalTo(1.toULong()))

        val deleteResult = db.deleteEvent(defaultEventId)
        assertThat(deleteResult, instanceOf(Success::class.java))
        assertThat((deleteResult as Success).value, equalTo(false))
        assertThat(db.getStats().eventCount, equalTo(0.toULong()))
    }

    @Test
    fun db_commit_no_wait() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        assertThat(db.getStats().eventCount, equalTo(0.toULong()))
        db.addEvent(event, profile)
        assertThat(db.getStats().eventCount, equalTo(0.toULong()))
        val commitResult = db.commit()
        assertThat(commitResult, instanceOf(Success::class.java))
        assertThat(db.getStats().eventCount, equalTo(1.toULong()))
    }

    @Test
    fun db_commit() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        assertThat(db.getStats().eventCount, equalTo(0.toULong()))
        db.addEvent(event, profile)
        assertThat(db.getStats().eventCount, equalTo(0.toULong()))
        db.commitSync()
        assertThat(db.getStats().eventCount, equalTo(1.toULong()))
    }

    @Test
    fun db_is_room_indexed() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        assertThat(db.getStats().eventCount, equalTo(0.toULong()))
        val notIndexed = db.isRoomIndexed("!test_room:localhost")
        assertThat(notIndexed, equalTo(false))
        db.addEvent(event, profile)
        db.commitSync()
        val indexed = db.isRoomIndexed("!test_room:localhost")
        assertThat(indexed, equalTo(true))
    }

    @Test
    fun db_add_historic_events() = runBlocking {
        val checkpoint = CrawlerCheckpoint.newCheckpoint(
            "!TESTROOM",
            "1234",
            false,
            Direction.Forwards
        )

        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        assertThat(db.getStats().eventCount, equalTo(0.toULong()))
        val addResult = db.addHistoricEvents(
            mapOf(event to profile),
            checkpoint,
            null
        )
        assertThat(addResult, instanceOf(Success::class.java))
        assertThat((addResult as Success).value, equalTo(false))
        db.reload()
        assertThat(db.getStats().eventCount, equalTo(1.toULong()))
        val searchBatch = db.search("Test", SearchConfig())
        assertThat(searchBatch, instanceOf(Success::class.java))
        assertThat((searchBatch as Success).value.count, equalTo(1))
        assertThat(searchBatch.value.results.size, equalTo(1))
        val searchResult = searchBatch.value.results[0]
        Log.w("EventResult","${searchResult.eventSource}")
        val res = when(val eventResult = Event.eventFromSource(searchResult.eventSource)){
            is Error -> Log.w(
                "EventResult",
                "${eventResult.reason} -> message: ${eventResult.reason.message}")
            is Success -> Log.w("EventResult","${eventResult.value}")
        }
    }

    @Test
    fun db_load_checkpoints() = runBlocking {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        assertThat(db.loadCheckpoints().size, equalTo(0))
        val checkpoint = CrawlerCheckpoint.newCheckpoint(
            "!TESTROOM",
            "1234",
            false,
            Direction.Forwards
        )
        db.addHistoricEvents(
            mapOf(event to profile),
            checkpoint,
            null
        )
        assertThat(db.loadCheckpoints().size, equalTo(1))
    }

    @Test
    fun search() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        val emptySearchBatch = db.search("Test", SearchConfig())
        assertThat(emptySearchBatch, instanceOf(Success::class.java))
        assertThat((emptySearchBatch as Success).value.count, equalTo(0))
        assertThat((emptySearchBatch).value.results.size, equalTo(0))

        db.addEvent(event, profile)
        db.addEvent(event2, profile)
        db.addEvent(event3, profile)

        db.forceCommit()
        db.reload()

        val johnSearchBatch = db.search("John", SearchConfig())
        assertThat(johnSearchBatch, instanceOf(Success::class.java))
        assertThat((johnSearchBatch as Success).value.count, equalTo(1))
        assertThat(johnSearchBatch.value.results.size, equalTo(1))
        val result = johnSearchBatch.value.results.first()
        assertThat(result.score.toDouble(), IsCloseTo(0.9808292, 0.01))
        val serializationResult = Event.eventFromSource(result.eventSource)
        if(serializationResult is Error){
            Log.w("Search test error" ,"${serializationResult.reason}-${serializationResult.reason.code}-${serializationResult.reason.message}")
        }else {
            val eventResult = (serializationResult as Success).value
            assertThat(eventResult.getEventId(), equalTo<String>(event.getEventId()))
            assertThat(eventResult.getContentValue(), equalTo<String>(event.getContentValue()))
            assertThat(eventResult.getEventType(), equalTo<EventType>(event.getEventType()))
            assertThat(eventResult.getMessageType(), equalTo<String?>(event.getMessageType()))
            assertThat(eventResult.getRoomId(), equalTo<String>(event.getRoomId()))
            assertThat(eventResult.getSender(), equalTo<String>(event.getSender()))
            assertThat(eventResult.getServerTs(), equalTo<Long>(event.getServerTs()))
        }


        val bobSearchBatch = db.search("Bob", SearchConfig())
        assertThat(bobSearchBatch, instanceOf(Success::class.java))
        assertThat((bobSearchBatch as Success).value.count, equalTo(1))
        assertThat(bobSearchBatch.value.results.size, equalTo(1))
        assertThat(johnSearchBatch.value.results.map {
            it.score.toDouble()
        }.first(), IsCloseTo(0.9808292, 0.01))

        val jimSearchBatch = db.search("Jim", SearchConfig())
        assertThat(jimSearchBatch, instanceOf(Success::class.java))
        assertThat((jimSearchBatch as Success).value.count, equalTo(1))
        assertThat(jimSearchBatch.value.results.size, equalTo(1))
        assertThat(johnSearchBatch.value.results.map {
            it.score.toDouble()
        }.first(), IsCloseTo(0.9808292, 0.01))

        val testSearchBatch = db.search("Test", SearchConfig())
        assertThat(testSearchBatch, instanceOf(Success::class.java))
        assertThat((testSearchBatch as Success).value.count, equalTo(3))
        assertThat(testSearchBatch.value.results.size, equalTo(3))
    }

    @Test
    fun search_with_config_limit() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        val emptySearchBatch = db.search("Test", SearchConfig())
        assertThat(emptySearchBatch, instanceOf(Success::class.java))
        assertThat((emptySearchBatch as Success).value.count, equalTo(0))
        assertThat((emptySearchBatch).value.results.size, equalTo(0))

        db.addEvent(event, profile)
        db.addEvent(event2, profile)
        db.addEvent(event3, profile)

        db.forceCommit()
        db.reload()

        val searchConfig = SearchConfig(2)
        val testSearchBatch = db.search("Test", searchConfig)
        assertThat(testSearchBatch, instanceOf(Success::class.java))
        assertThat((testSearchBatch as Success).value.results.size, equalTo(2))
    }

    @Test
    fun free_db() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val result = Database.get(appContext.filesDir.path) as Success
        result.value.testFinalize()
        //Shouldn't crash on 'double free'
        result.value.testFinalize()
    }

}
