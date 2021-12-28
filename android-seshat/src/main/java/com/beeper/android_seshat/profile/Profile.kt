package com.beeper.android_seshat.profile

import com.beeper.android_seshat.LibraryLoader.ensureNativeLibIsLoaded

class Profile internal constructor(ptr: Long){
    internal val ptr:Long

    constructor(displayName : String, avatarURL : String) : this(
        n_new_profile(displayName, avatarURL)
    )

    init{
        ensureNativeLibIsLoaded()
        this.ptr = ptr
    }

    /*
     * Called if the object is GC'd by the JVM
     */
    protected fun finalize() {
        n_free_profile(ptr)
    }

    internal fun testFinalize(){
        finalize()
    }

    private external fun n_free_profile(profilePointer: Long)

    companion object{
        @JvmStatic
        private external fun n_new_profile(displayName : String, avatarURL : String): Long
    }
}

