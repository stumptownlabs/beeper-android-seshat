package com.beeper.android_seshat.database

import com.beeper.android_seshat.LibraryLoader.ensureNativeLibIsLoaded
import com.beeper.android_seshat.event.CrawlerCheckpoint
import com.beeper.android_seshat.event.Event
import com.beeper.android_seshat.event.NativeEventList
import com.beeper.android_seshat.event.NativeFileEventList
import com.beeper.android_seshat.profile.Profile
import com.beeper.android_seshat.search.SearchBatch
import com.beeper.android_seshat.search.SearchConfig
import com.beeper.android_seshat.search.SearchErrorType
import com.beeper.android_seshat.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Database private constructor(private val ptr: Long){
    private var state : DatabaseState = Open

    companion object {
        fun get(dirPath: String): Result<Database, DatabaseErrorType> {
            ensureNativeLibIsLoaded()

            val nativeDatabaseResult =
                NativeResult()
            n_new_database(dirPath, nativeDatabaseResult)

            val errorCode = nativeDatabaseResult.errorCode
            if (errorCode >= 0) {
                val errorMessage = nativeDatabaseResult.errorMessage
                return Error(DatabaseErrorType.fromCode(errorCode, errorMessage))
            }

            return Success(Database(nativeDatabaseResult.resultPtr))
        }

        fun getWithConfig(
            dirPath: String,
            config: DatabaseConfig
        ): Result<Database, DatabaseErrorType> {
            ensureNativeLibIsLoaded()

            val nativeDatabaseResult =
                NativeResult()
            n_new_database_with_config(dirPath, config.ptr, nativeDatabaseResult)

            val errorCode = nativeDatabaseResult.errorCode
            if (errorCode >= 0) {
                val errorMessage = nativeDatabaseResult.errorMessage
                return Error(DatabaseErrorType.fromCode(errorCode, errorMessage))
            }

            return Success(Database(nativeDatabaseResult.resultPtr))
        }

        @JvmStatic
        private external fun n_new_database(dirPath: String, result: NativeResult)

        @JvmStatic
        private external fun n_new_database_with_config(
            dirPath: String,
            configPtr: Long,
            result: NativeResult
        )
    }

    fun addEvent(event: Event, profile: Profile) {
        throwIfClosed()
        n_add_event(ptr, event.ptr, profile.ptr)
    }

    fun change_passphrase(newPassphrase: String) {
        throwIfClosed()

        markDatabaseAsClosed()
        n_change_passphrase(ptr, newPassphrase)
    }

    fun getSize() : ULong {
        throwIfClosed()

        return n_get_size(ptr).toULong()
    }

    suspend fun deleteEvent(eventId: String) : Result<Boolean,DatabaseErrorType> =
        suspendCoroutine { cont ->
            throwIfClosed()

            val callback = object : NativeAsyncResultCallback<Boolean> {
                override fun onResult(value: Boolean) {
                    cont.resume(Success(value))
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    var error = DatabaseErrorType.fromCode(errorCode,errorMessage)
                    cont.resume(Error(error))
                }
            }
            n_delete_event(ptr, eventId, callback)
        }

    fun commitSync() {
        throwIfClosed()

        n_commit_sync(ptr)
    }

    suspend fun commit() : Result<Unit,DatabaseErrorType> =
        suspendCoroutine { cont ->
            throwIfClosed()

            val callback = object : NativeAsyncResultCallback<Boolean> {
                override fun onResult(value: Boolean) {
                    cont.resume(Success(Unit))
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    var error = DatabaseErrorType.fromCode(errorCode,errorMessage)
                    cont.resume(Error(error))
                }
            }
            n_commit_no_wait(ptr, callback)
        }

    internal fun forceCommit() {
        throwIfClosed()

        n_force_commit(ptr)
    }

    internal fun reload() {
        throwIfClosed()

        n_reload(ptr)
    }

    fun isRoomIndexed(roomId: String) : Boolean {
        throwIfClosed()

        return n_is_room_indexed(ptr,roomId)
    }

    fun search(term: String, searchConfig: SearchConfig): Result<SearchBatch, SearchErrorType> {
        throwIfClosed()

        val nativeSearchResult = NativeResult()
        n_search(ptr, term, searchConfig.ptr, nativeSearchResult)
        val errorCode = nativeSearchResult.errorCode
        if (errorCode >= 0) {
            return Error(SearchErrorType.fromCode(errorCode))
        }
        return Success(SearchBatch(nativeSearchResult.resultPtr))
    }

    fun getStats() : DatabaseStats {
        throwIfClosed()
        return DatabaseStats(
            n_get_database_stats(ptr)
        )
    }

    suspend fun addHistoricEvents(events : Map<Event,Profile>,
                          newCheckpoint: CrawlerCheckpoint?,
                          oldCheckpoint: CrawlerCheckpoint?
    ) : Result<Boolean,DatabaseErrorType> =
        suspendCoroutine { cont ->
            throwIfClosed()
            val callback = object : NativeAsyncResultCallback<Boolean> {
                override fun onResult(value: Boolean) {
                    cont.resume(Success(value))
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    var error = DatabaseErrorType.fromCode(errorCode,errorMessage)
                    cont.resume(Error(error))
                }
            }
            val nativeEventList = NativeEventList(events)
            val nativeNewCheckpoint = if(newCheckpoint != null){
                NativeSome(newCheckpoint.ptr)
            }else{
                NativeNone
            }
            val nativeOldCheckpoint = if(oldCheckpoint != null){
                NativeSome(oldCheckpoint.ptr)
            }else{
                NativeNone
            }
            n_add_historic_events(
                ptr,
                nativeEventList.getEvents(),
                nativeEventList.getProfiles(),
                nativeNewCheckpoint,
                nativeOldCheckpoint,
                callback
            )
        }

    suspend fun addCrawlerCheckpoint(checkpoint: CrawlerCheckpoint) : Result<Boolean,DatabaseErrorType> {
            throwIfClosed()

            return addHistoricEvents(mapOf(),checkpoint,null)
    }

    suspend fun removeCrawlerCheckpoint(checkpoint: CrawlerCheckpoint) : Result<Boolean,DatabaseErrorType> {
        throwIfClosed()

        return addHistoricEvents(mapOf(),null,checkpoint)
    }

    fun loadCheckpoints() : List<CrawlerCheckpoint> {
        throwIfClosed()

        return n_load_checkpoints(ptr).map {
            ptr ->
            CrawlerCheckpoint(ptr)
        }
    }

    fun delete(){
        throwIfClosed()

        markDatabaseAsClosed()
        n_delete(ptr)
    }

    suspend fun shutdown(): Result<Unit,DatabaseErrorType> =
        suspendCoroutine { cont ->
            throwIfClosed()

            val callback = object : NativeAsyncResultCallback<Boolean> {
                override fun onResult(value: Boolean) {
                    markDatabaseAsClosed()
                    cont.resume(Success(Unit))
                }

                override fun onError(errorCode: Int, errorMessage: String) {
                    var error = DatabaseErrorType.fromCode(errorCode, errorMessage)
                    cont.resume(Error(error))
                }
            }
            n_shutdown(ptr, callback)
        }

     suspend fun isEmpty(): Result<Boolean,DatabaseErrorType> = suspendCoroutine{
         cont ->
         throwIfClosed()

         val callback = object : NativeAsyncResultCallback<Boolean> {
             override fun onResult(value: Boolean) {
                 cont.resume(Success(value))
             }

             override fun onError(errorCode: Int, errorMessage: String) {
                 var error = DatabaseErrorType.fromCode(errorCode, errorMessage)
                 cont.resume(Error(error))
             }
         }
         n_is_empty(ptr,callback)
     }

    fun getUserVersion(): Long {
        throwIfClosed()

        return n_get_user_version(ptr)
    }

    fun setUserVersion(userVersion: Long) {
        throwIfClosed()

        n_set_user_version(ptr, userVersion)
    }

    fun loadFileEvents(loadConfig: LoadConfig): List<Pair<String,Profile>> {
        throwIfClosed()

        val eventListResult = NativeFileEventList()
        n_load_file_events(ptr,
            loadConfig.roomId,
            loadConfig.limit,
            loadConfig.direction.code,
            loadConfig.fromEvent!=null,
            loadConfig.fromEvent ?: String(),
            eventListResult
        )
        return eventListResult.getResult()
    }

    /*
     * After moved pointer, it should close the database, making the pointer unavailable.
     * The lib panics if it doesn't have an existing passphrase in config
     */
    private fun markDatabaseAsClosed(){
        state = Closed
    }

    private fun throwIfClosed(){
        if(state is Closed) throw Exception("Trying to access a closed Database!")
    }

    /*
     * Called if the object is GC'd by the JVM
     */
    protected fun finalize() {
        if (state !is Closed) {
            markDatabaseAsClosed()
            n_free_database(ptr)
        }
    }

    // For testing purposes only
    internal fun testFinalize(){
        finalize()
    }

    private external fun n_add_event(
        databasePointer: Long,
        eventPointer: Long,
        profilePointer: Long
    )

    private external fun n_change_passphrase(
        databasePointer: Long,
        newPassphrase: String,
    )


    private external fun n_get_size(
        databasePointer: Long,
    ) : Long


    private external fun n_commit_sync(databasePointer: Long)
    private external fun n_commit_no_wait(databasePointer: Long,
                                          resultCallback: NativeAsyncResultCallback<Boolean>
    )
    private external fun n_force_commit(databasePointer: Long)
    private external fun n_reload(databasePointer: Long)
    private external fun n_is_room_indexed(databasePointer: Long, roomId: String) : Boolean

    private external fun n_search(
        databasePointer: Long,
        term: String,
        searchConfigPointer: Long,
        nativeResult: NativeResult
    )

    private external fun n_add_historic_events(databasePointer: Long,
                                               eventArray: LongArray,
                                               profileArray: LongArray,
                                               newCheckpoint: NativeOption,
                                               oldCheckpoint: NativeOption,
                                               resultCallback: NativeAsyncResultCallback<Boolean>
    )

    private external fun n_load_checkpoints(databasePointer: Long) : LongArray

    private external fun n_delete_event(databasePointer: Long, eventId: String,
                                        resultCallback: NativeAsyncResultCallback<Boolean>
    ): String
    private external fun n_get_database_stats(databasePointer: Long): Long
    private external fun n_delete(databasePointer: Long)
    private external fun n_is_empty(databasePointer: Long,
                                    resultCallback: NativeAsyncResultCallback<Boolean>
    )

    private external fun n_shutdown(
        databasePointer: Long,
        resultCallback: NativeAsyncResultCallback<Boolean>
    )

    private external fun n_get_user_version(
        databasePointer: Long,
    ) : Long

    private external fun n_set_user_version(
        databasePointer: Long,
        userVersion: Long
    )

    private external fun n_load_file_events(
        databasePointer: Long,
        roomId: String,
        limit: Int,
        direction: Int,
        hasFromEvent: Boolean = false,
        fromEvent: String,
        fileEventList: NativeFileEventList
    )

    private external fun n_free_database(databasePointer: Long)
}



