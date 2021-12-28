package com.beeper.android_seshat.event

import com.beeper.android_seshat.LibraryLoader.ensureNativeLibIsLoaded


class CrawlerCheckpoint internal constructor(internal val ptr:Long){

    /*
     * Called if the object is GC'd by the JVM
     */
    protected fun finalize() {
        n_free_crawler_checkpoint(ptr)
    }

    internal fun testFinalize(){
        finalize()
    }

    fun getRoomId() : String{
        return n_get_room_id(ptr)
    }

    fun getToken() : String{
        return n_get_token(ptr)
    }

    fun getFullCrawl(): Boolean{
        return n_get_full_crawl(ptr)
    }

    fun getDirection(): Direction {
        return Direction.fromCode(n_get_direction(ptr))
    }

    private external fun n_free_crawler_checkpoint(crawlerCheckpointPointer: Long)

    companion object{
        fun newCheckpoint(roomId : String, token : String, fullCrawl: Boolean, direction: Direction) : CrawlerCheckpoint{
            ensureNativeLibIsLoaded()
            return CrawlerCheckpoint(  n_new_checkpoint(
                roomId, token, fullCrawl, direction.code
            ))
        }
        @JvmStatic
        private external fun n_new_checkpoint(
            roomId : String, token : String, fullCrawl: Boolean, direction: Int
        ): Long

        @JvmStatic
        private external fun n_get_room_id(crawlerCheckpointPointer: Long): String
        @JvmStatic
        private external fun n_get_token(crawlerCheckpointPointer: Long): String
        @JvmStatic
        private external fun n_get_full_crawl(crawlerCheckpointPointer: Long): Boolean
        @JvmStatic
        private external fun n_get_direction(crawlerCheckpointPointer: Long): Int
    }
}




