package com.beeper.android_seshat.search

import java.lang.Integer.MAX_VALUE

enum class SearchErrorType(val code : Int){
    SearchError(0),
    UnknownError(MAX_VALUE);

    companion object {
        private val types = values().associateBy { it.code }
        fun fromCode(value: Int) = types[value] ?: UnknownError
    }
}
