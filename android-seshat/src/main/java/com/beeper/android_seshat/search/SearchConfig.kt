package com.beeper.android_seshat.search

import com.beeper.android_seshat.LibraryLoader.ensureNativeLibIsLoaded
import com.beeper.android_seshat.event.EventType


class SearchConfig internal constructor(ptr: Long) {
    internal val ptr: Long

    init {
        ensureNativeLibIsLoaded()
        this.ptr = ptr
    }

    constructor(
        limit: Int = 10, beforeLimit: Int = 0, afterLimit: Int = 0, orderByRecency: Boolean = false,
        roomId: String? = null, keys: List<EventType> = listOf(), nextBatch: String? = null
    ) : this(
        buildSearchConfig(limit, beforeLimit, afterLimit, orderByRecency, roomId, keys, nextBatch)
    )

    /*
     * Called if the object is GC'd by the JVM
     */
    protected fun finalize() {
        n_free_search_config(ptr)
    }

    internal fun testFinalize(){
        finalize()
    }

    private external fun n_free_search_config(searchConfig: Long)

    companion object {
        private fun buildSearchConfig(
            limit: Int = 10,
            beforeLimit: Int = 0,
            afterLimit: Int = 0,
            orderByRecency: Boolean = false,
            roomId: String? = null,
            keys: List<EventType> = listOf(),
            nextBatch: String? = null
        ): Long {

            val ptr = n_new_search_config()
            n_search_config_set_limit(ptr,limit)
            n_search_config_set_before_limit(ptr, beforeLimit)
            n_search_config_set_after_limit(ptr, afterLimit)
            n_search_config_set_order_by_recency(ptr, orderByRecency)
            roomId?.apply {
                n_search_config_set_room_id(ptr, this)
            }
            keys.onEach {
                n_search_config_set_with_key(ptr, it.code)
            }
            nextBatch?.apply {
                n_search_config_set_next_batch(ptr, this)
            }

            return ptr
        }

        @JvmStatic
        private external fun n_new_search_config(): Long

        @JvmStatic
        private external fun n_search_config_set_limit(
            searchConfigPointer: Long,
            limit: Int,
        )

        @JvmStatic
        private external fun n_search_config_set_before_limit(
            searchConfigPointer: Long,
            before_limit: Int
        )

        @JvmStatic
        private external fun n_search_config_set_after_limit(
            searchConfigPointer: Long,
            after_limit: Int
        )

        @JvmStatic
        private external fun n_search_config_set_order_by_recency(
            searchConfigPointer: Long,
            order_by_recency: Boolean
        ): Long

        @JvmStatic
        private external fun n_search_config_set_room_id(
            searchConfigPointer: Long,
            roomId: String
        ): Long

        @JvmStatic
        private external fun n_search_config_set_with_key(
            searchConfigPointer: Long,
            keys: Int
        ): Long

        @JvmStatic
        private external fun n_search_config_set_next_batch(
            searchConfigPointer: Long,
            next_batch: String
        ): Long
    }
}






