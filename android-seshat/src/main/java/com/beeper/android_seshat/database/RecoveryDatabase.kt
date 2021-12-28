package com.beeper.android_seshat.database

import com.beeper.android_seshat.LibraryLoader
import com.beeper.android_seshat.util.*
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// Open a read-only Seshat Database
class RecoveryDatabase private constructor(private val ptr: Long) {
    private var state : DatabaseState = Open

    fun info() : RecoveryInfo{
        throwIfClosed()
        return RecoveryInfo(n_get_info(ptr))
    }

    fun getUserVersion(): Long {
        throwIfClosed()
        return n_get_user_version(ptr)
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
                    val error = DatabaseErrorType.fromCode(errorCode, errorMessage)
                    cont.resume(Error(error))
                }
            }
            n_shutdown(ptr, callback)
        }

    fun reindex() {
        throwIfClosed()
        n_reindex(ptr)
        markDatabaseAsClosed()
    }

    /*
     * After moved pointer, it should close the database, making the pointer unavailable.
     * The lib panics if it doesn't have an existing passphrase in config
     */
    private fun markDatabaseAsClosed(){
        state = Closed
    }

    private fun throwIfClosed(){
        if(state is Closed) throw Exception("Trying to access a closed RecoveryDatabase!")
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

    internal fun testFinalize(){
        finalize()
    }

    companion object {
        fun newInstance(dirPath: String): Result<RecoveryDatabase, DatabaseErrorType> {
            LibraryLoader.ensureNativeLibIsLoaded()

            val nativeDatabaseResult =
                NativeResult()
            n_new_database(dirPath, nativeDatabaseResult)

            val errorCode = nativeDatabaseResult.errorCode
            if (errorCode >= 0) {
                val errorMessage = nativeDatabaseResult.errorMessage
                return Error(DatabaseErrorType.fromCode(errorCode, errorMessage))
            }

            return Success(RecoveryDatabase(nativeDatabaseResult.resultPtr))
        }

        fun newInstanceWithConfig(
            dirPath: String,
            config: DatabaseConfig
        ): Result<RecoveryDatabase, DatabaseErrorType> {
            LibraryLoader.ensureNativeLibIsLoaded()

            val nativeDatabaseResult =
                NativeResult()
            n_new_database_with_config(dirPath, config.ptr, nativeDatabaseResult)

            val errorCode = nativeDatabaseResult.errorCode
            if (errorCode >= 0) {
                val errorMessage = nativeDatabaseResult.errorMessage

                return Error(DatabaseErrorType.fromCode(errorCode, errorMessage))
            }

            return Success(RecoveryDatabase(nativeDatabaseResult.resultPtr))
        }

        @JvmStatic
        private external fun n_get_info(recoveryDatabasePointer: Long) : Long

        @JvmStatic
        private external fun n_new_database(dirPath: String, result: NativeResult)

        @JvmStatic
        private external fun n_get_user_version(
            recoveryDatabasePointer: Long,
        ) : Long

        @JvmStatic
        private external fun n_shutdown(
            recoveryDatabasePointer: Long,
            resultCallback: NativeAsyncResultCallback<Boolean>
        )

        @JvmStatic
        private external fun n_reindex(
            recoveryDatabasePointer: Long,
        )

        @JvmStatic
        private external fun n_new_database_with_config(
            dirPath: String,
            configPtr: Long,
            result: NativeResult
        )
    }

    private external fun n_free_database(databasePointer: Long)

}
