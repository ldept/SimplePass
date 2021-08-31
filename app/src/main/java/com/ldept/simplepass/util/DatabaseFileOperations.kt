package com.ldept.simplepass.util

import java.io.File
import java.io.InputStream
import java.io.OutputStream

object DatabaseFileOperations {
    fun copyFile(from : InputStream, to : OutputStream){
        from.use { input ->
            to.use { output ->
                output.let { input.copyTo(it) }
            }
        }
    }

    fun removeWALAndSHM(databaseFile : File) {
        val dbShm = File(databaseFile.path + "-shm")
        val dbWal = File(databaseFile.path + "-wal")
        if (dbShm.exists()) dbShm.delete()
        if (dbWal.exists()) dbWal.delete()
    }
}