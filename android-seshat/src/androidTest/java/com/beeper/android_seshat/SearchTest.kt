package com.beeper.android_seshat

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import com.beeper.android_seshat.database.Database
import com.beeper.android_seshat.event.CrawlerCheckpoint
import com.beeper.android_seshat.event.Direction
import com.beeper.android_seshat.search.SearchConfig
import com.beeper.android_seshat.util.Success


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class SearchTest {
    @Test
    fun create_and_free_search_batch() = runBlocking{
        val checkpoint = CrawlerCheckpoint.newCheckpoint(
            "!TESTROOM",
            "1234",
            false,
            Direction.Forwards
        )

        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (Database.get(appContext.filesDir.path) as Success).value
        db.addHistoricEvents(
            mapOf(event to profile),
            checkpoint,
            null
        )
        db.reload()
        val searchBatch = db.search("John", SearchConfig())
        MatcherAssert.assertThat(searchBatch, CoreMatchers.instanceOf(Success::class.java))
        MatcherAssert.assertThat((searchBatch as Success).value.count, CoreMatchers.equalTo(1))
        MatcherAssert.assertThat(searchBatch.value.results.size, CoreMatchers.equalTo(1))
        searchBatch.value.results.onEach {
            it.testFinalize()
        }
        searchBatch.value.testFinalize()
    }

}
