package com.beeper.android_seshat

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import com.beeper.android_seshat.event.CrawlerCheckpoint
import com.beeper.android_seshat.event.Direction


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CrawlerCheckpointTest {
    @Test
    fun create_crawler_checkpoint()
    {
        CrawlerCheckpoint.newCheckpoint(
            "!TESTROOM",
            "1234",
            false,
            Direction.Forwards
        )
    }


    @Test
    fun crawler_checkpoint_get_room_id()
    {
        val testRoomId = "!TESTROOM"
        val roomId = CrawlerCheckpoint.newCheckpoint(
            testRoomId,
            "1234",
            false,
            Direction.Forwards
        ).getRoomId()
        MatcherAssert.assertThat(roomId, CoreMatchers.equalTo(testRoomId))
    }

    @Test
    fun crawler_checkpoint_get_token()
    {
        val testToken = "1234"
        val token = CrawlerCheckpoint.newCheckpoint(
            "!TESTROOM",
            testToken,
            false,
            Direction.Forwards
        ).getToken()
        MatcherAssert.assertThat(token, CoreMatchers.equalTo(testToken))
    }

    @Test
    fun crawler_checkpoint_get_full_crawl()
    {
        val testFullCrawl = true
        val fullCrawl = CrawlerCheckpoint.newCheckpoint(
            "!TESTROOM",
            "1234",
            testFullCrawl,
            Direction.Forwards
        ).getFullCrawl()
        MatcherAssert.assertThat(fullCrawl, CoreMatchers.equalTo(testFullCrawl))
    }

    @Test
    fun crawler_checkpoint_get_direction()
    {
        val testDirection = Direction.Forwards
        val direction = CrawlerCheckpoint.newCheckpoint(
            "!TESTROOM",
            "1234",
            true,
            testDirection
        ).getDirection()
        MatcherAssert.assertThat(direction, CoreMatchers.equalTo(testDirection))
    }


    @Test
    fun free_crawler_checkpoint()
    {
        val checkpoint = CrawlerCheckpoint.newCheckpoint(
            "!TESTROOM",
            "1234",
            false,
            Direction.Forwards
        )
        checkpoint.testFinalize()
    }
}
