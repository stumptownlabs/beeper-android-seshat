package com.beeper.android_seshat

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import com.beeper.android_seshat.profile.Profile


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ProfileTest {
    @Test
    fun create_profile()
    {
        val displayName = "Alice"
        val avatarURL = "https://test.com"
        Profile(displayName,avatarURL)
    }

    @Test
    fun free_profile()
    {
        val displayName = "Alice"
        val avatarURL = "https://test.com"
        val profile = Profile(displayName,avatarURL)
        profile.testFinalize()
    }
}
