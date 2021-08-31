package com.ldept.simplepass.database

import android.content.Context
import android.os.strictmode.InstanceCountViolation
import androidx.room.Database
import androidx.room.RawQuery
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory


@Database(entities = [PasswordEntry::class], version = 1)
abstract class PasswordDatabase : RoomDatabase() {
    abstract fun passwordEntryDAO() : PasswordEntryDAO

    companion object {
        @Volatile
        private var INSTANCE : PasswordDatabase? = null

        fun getDatabase(context : Context, pass : String) : PasswordDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory = SupportFactory(SQLiteDatabase.getBytes(pass.toCharArray()), null, false)
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        PasswordDatabase::class.java,
                        "password_database_encrypted"
                ).openHelperFactory(factory).build()


                INSTANCE = instance
                instance
            }
        }
        private fun isDatabaseOpened() : Boolean {
            return INSTANCE?.isOpen == true
        }
        fun closeDatabase() {
            if(isDatabaseOpened())
                INSTANCE!!.openHelper.close()
            INSTANCE = null
        }

        fun reopenDatabase() {
            INSTANCE?.openHelper?.writableDatabase
        }



    }
}