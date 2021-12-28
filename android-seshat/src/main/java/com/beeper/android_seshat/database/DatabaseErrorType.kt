package com.beeper.android_seshat.database

import java.lang.Integer.MAX_VALUE

sealed class DatabaseErrorType(val code: Int, val message: String){
    companion object {
        fun fromCode(value: Int, message: String) : DatabaseErrorType {
            return when(value){
                0 -> PoolError(message)
                1 -> DatabaseError(message)
                2 -> IndexError(message)
                3 -> FsError(message)
                4 -> IOError(message)
                5 -> DatabaseUnlockError(message)
                6 -> DatabaseVersionError(message)
                7 -> DatabaseOpenError(message)
                8 -> SqlCipherError(message)
                9 -> ReindexError(message)
                10 -> RecvError(message)
                else -> UnknownError(message)
            }
        }
    }
}

class PoolError(message:String) : DatabaseErrorType(0,message)
class DatabaseError(message:String) : DatabaseErrorType(1,message)
class IndexError(message:String) : DatabaseErrorType(2,message)
class FsError(message:String) : DatabaseErrorType(3,message)
class IOError(message:String) : DatabaseErrorType(4,message)
class DatabaseUnlockError(message: String) : DatabaseErrorType(5, message)
class DatabaseVersionError(message:String) : DatabaseErrorType(6,message)
class DatabaseOpenError(message: String) : DatabaseErrorType(7,message)
class SqlCipherError(message: String) : DatabaseErrorType(8,message)
class ReindexError(message:String) : DatabaseErrorType(9,message)
class RecvError(message:String) : DatabaseErrorType(10,message)
class UnknownError(message:String) : DatabaseErrorType(MAX_VALUE,message)
