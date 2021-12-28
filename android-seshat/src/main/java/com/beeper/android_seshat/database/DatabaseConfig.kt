package com.beeper.android_seshat.database

import com.beeper.android_seshat.LibraryLoader.ensureNativeLibIsLoaded


class DatabaseConfig internal constructor(ptr: Long) {
    internal val ptr: Long

    init {
        ensureNativeLibIsLoaded()
        this.ptr = ptr
    }

    constructor(
        language: DatabaseLanguage, passphrase: String
    ) : this(
        n_new_database_config(
            language.code, passphrase
        )
    )

    /*
     * Called if the object is GC'd by the JVM
     */
    protected fun finalize() {
        n_free_database_config(ptr)
    }

    private external fun n_free_database_config(ptr: Long)

    companion object {
        @JvmStatic
        private external fun n_new_database_config(
            language: Int, passphrase: String
        ): Long
    }
}




