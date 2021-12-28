package com.beeper.android_seshat.search

import com.beeper.android_seshat.LibraryLoader.ensureNativeLibIsLoaded

class SearchBatch internal constructor(private val ptr:Long){

    init{
        ensureNativeLibIsLoaded()
    }

    val nextBatch: String? get() = n_search_batch_get_next_batch(ptr).let {
        if(it.isNotEmpty()){
            it
        }else{
            null
        }
    }
    val count: Int get() = n_search_batch_get_count(ptr)
    val results: List<SearchResult> get() = resultsArrayToResults()

    private fun resultsArrayToResults() : List<SearchResult>{
        val nativeSearchResults = NativeSearchResults()
        n_search_batch_get_results(ptr, nativeSearchResults)
        return nativeSearchResults.array.map {
            SearchResult(it)
        }.toList()
    }

    private external fun n_search_batch_get_next_batch(searchBatchPointer:Long) : String
    private external fun n_search_batch_get_count(searchBatchPointer:Long) : Int
    private external fun n_search_batch_get_results(searchBatchPointer:Long, resultArray: NativeSearchResults)

    /*
     * Called if the object is GC'd by the JVM
     */
    protected fun finalize() {
        n_free_search_batch(ptr)
    }

    internal fun testFinalize(){
        finalize()
    }

    private external fun n_free_search_batch(searchBatchPointer: Long)
}

