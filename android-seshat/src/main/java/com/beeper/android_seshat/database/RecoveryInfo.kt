package com.beeper.android_seshat.database

import com.beeper.android_seshat.LibraryLoader

class RecoveryInfo internal constructor(ptr: Long) {
    private val ptr:Long

    val totalEventCount: ULong get() = n_recovery_info_get_total_event_count(ptr).toULong()
    val reindexed_events: ULong get() = n_recovery_info_get_reindexed_events(ptr).toULong()

    init{
        LibraryLoader.ensureNativeLibIsLoaded()
        this.ptr = ptr
    }

    /*
     * Called if the object is GC'd by the JVM
     */
    protected fun finalize() {
        n_free_recovery_info(ptr)
    }

    internal fun testFinalize(){
        finalize()
    }

    private external fun n_free_recovery_info(ptr: Long)

    private external fun n_recovery_info_get_total_event_count(
        databaseStatsPointer: Long,
    ): Long

    private external fun n_recovery_info_get_reindexed_events(
        databaseStatsPointer: Long,
    ): Long


}
