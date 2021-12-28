package com.beeper.android_seshat.database

import com.beeper.android_seshat.LibraryLoader

class DatabaseStats internal constructor(ptr: Long) {

    private val ptr:Long

    val size: ULong = n_database_stats_get_size(ptr).toULong()
    val eventCount: ULong = n_database_stats_get_event_count(ptr).toULong()
    val roomCount: ULong = n_database_stats_get_room_count(ptr).toULong()

    init{
        LibraryLoader.ensureNativeLibIsLoaded()
        this.ptr = ptr
    }


    /*
     * Called if the object is GC'd by the JVM
     */
    protected fun finalize() {
        n_free_database_stats(ptr)
    }

    private external fun n_free_database_stats(ptr: Long)

    private external fun n_database_stats_get_size(
        databaseStatsPointer: Long,
    ): Long

    private external fun n_database_stats_get_event_count(
        databaseStatsPointer: Long,
    ): Long

    private external fun n_database_stats_get_room_count(
        databaseStatsPointer: Long,
    ): Long

}
