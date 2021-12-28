package com.beeper.android_seshat

import java.io.File

object Utils{
    fun deleteFilesFromFolder(pathName:String){
        val dir = File(pathName)
        if (dir.isDirectory) {
            dir.list()?.onEach {
                filePathName ->
                File(dir, filePathName).delete()
            }
        }
    }
}