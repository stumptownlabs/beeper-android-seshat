package com.beeper.android_seshat.database

import java.lang.Integer.MAX_VALUE

enum class DatabaseLanguage(val code : Int){
    Arabic(0),
    Danish(1),
    Dutch(2),
    English(3),
    Finnish(4),
    French(5),
    German(6),
    Greek(7),
    Hungarian(8),
    Italian(9),
    Portuguese(10),
    Romanian(11),
    Russian(12),
    Spanish(13),
    Swedish(14),
    Tamil(15),
    Turkish(16),
    Japanese(17),
    Unknown(MAX_VALUE);

    companion object {
        private val types = values().associateBy { it.code }
        fun fromCode(value: Int) = types[value] ?: Unknown
    }
}