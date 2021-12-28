package com.beeper.android_seshat.search

import com.beeper.android_seshat.LibraryLoader
import com.beeper.android_seshat.event.NativeSerializedEventsResult
import com.beeper.android_seshat.profile.NativeProfileInfoResult
import com.beeper.android_seshat.profile.Profile
import com.beeper.android_seshat.profile.ProfileInfo

class SearchResult internal constructor(private val ptr:Long){

    init{
        LibraryLoader.ensureNativeLibIsLoaded()
    }

    val score = n_search_result_get_score(ptr)
    val eventSource : String get() = n_search_result_get_event_source(ptr)
    val eventsBefore: List<String> get() = resultsArrayToEventsBeforeListResults()
    val eventsAfter: List<String> get() = resultsArrayToEventsAfterListResults()
    val profileInfo: ProfileInfo get()  {
        val nativeProfileInfoResult = NativeProfileInfoResult()
        n_search_result_get_profile_info(ptr,nativeProfileInfoResult)
        return ProfileInfo(nativeProfileInfoResult.info.mapValues{
            Profile(it.value)
        })
    }

    private fun resultsArrayToEventsBeforeListResults() : List<String>{
        val results = NativeSerializedEventsResult()
        n_search_result_get_events_before(ptr, results)
        return results.array.map {
            //Event(it)
            it
        }.toList()
    }

    private fun resultsArrayToEventsAfterListResults() : List<String>{
        val results = NativeSerializedEventsResult()
        n_search_result_get_events_after(ptr, results)
        return results.array.map {
            it
        }.toList()
    }

    /*
     * Called if the object is GC'd by the JVM
     */
    protected fun finalize() {
        n_free_search_result(ptr)
    }

    internal fun testFinalize(){
        finalize()
    }

    private external fun n_free_search_result(searchBatchPointer: Long)

    private external fun n_search_result_get_score(searchResultPointer:Long) : Float
    private external fun n_search_result_get_event_source(searchResultPointer:Long) : String
    private external fun n_search_result_get_events_before(searchResultPointer:Long, resultArray: NativeSerializedEventsResult)
    private external fun n_search_result_get_events_after(searchResultPointer:Long, resultArray: NativeSerializedEventsResult)
    private external fun n_search_result_get_profile_info(searchResultPointer:Long, nativeProfileInfoResult: NativeProfileInfoResult) : Long


}

