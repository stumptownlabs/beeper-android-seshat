package com.beeper.android_seshat

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.beeper.android_seshat.Utils.deleteFilesFromFolder
import com.beeper.android_seshat.database.DatabaseConfig
import com.beeper.android_seshat.database.DatabaseLanguage
import com.beeper.android_seshat.database.RecoveryDatabase
import com.beeper.android_seshat.util.Success


/**
 * Instrumented test, which will execute on an Android device.
 */

@RunWith(AndroidJUnit4::class)
class RecoveryDatabaseTest {

    @Before
    fun setUp() {
        //Always delete the database before starting a new test
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        deleteFilesFromFolder(appContext.filesDir.path)
    }

    @Test
    fun create_recovery_db() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val result = RecoveryDatabase.newInstance(appContext.filesDir.path)
        assertThat(result, instanceOf(Success::class.java))
    }

    @Test
    fun create_encrypted_recovery_db_with_config() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseConfig = DatabaseConfig(DatabaseLanguage.English, "testPassphrase")
        val result = RecoveryDatabase.newInstanceWithConfig(appContext.filesDir.path, databaseConfig)
        assertThat(result, instanceOf(Success::class.java))
        //Returning SQLCipherError while we don't bundle SQLCipher
    }

    @Test
    fun recovery_db_get_info() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val result = RecoveryDatabase.newInstance(appContext.filesDir.path)
        assertThat(result, instanceOf(Success::class.java))
        val info = (result as Success).value.info()
        assertThat(info.totalEventCount, equalTo(0.toULong()))

    }

    @Test
    fun recovery_db_free_recovery_info() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val result = RecoveryDatabase.newInstance(appContext.filesDir.path)
        assertThat(result, instanceOf(Success::class.java))
        val info = (result as Success).value.info()
        assertThat(info.totalEventCount, equalTo(0.toULong()))
        info.testFinalize()
    }

    @Test
    fun recovery_db_get_user_version() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (RecoveryDatabase.newInstance(appContext.filesDir.path) as Success).value
        assertThat(db.getUserVersion(), equalTo(0L))
    }

    @Test
    fun recovery_db_shutdown() = runBlocking{
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (RecoveryDatabase.newInstance(appContext.filesDir.path) as Success).value
        val result = db.shutdown()
        assertThat(result, instanceOf(Success::class.java))
    }

    @Test
    fun recovery_db_reindex() = runBlocking{
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (RecoveryDatabase.newInstance(appContext.filesDir.path) as Success).value
        db.reindex()
    }

    @Test
    fun free_recovery_db() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val db = (RecoveryDatabase.newInstance(appContext.filesDir.path) as Success).value
        db.testFinalize()
        //Shouldn't crash on 'double free'
        db.testFinalize()
    }

}
