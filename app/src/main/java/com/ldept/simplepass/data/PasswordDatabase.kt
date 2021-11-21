package com.ldept.simplepass.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import androidx.room.RoomDatabase
import com.ldept.simplepass.data.Entities.PasswordEntry


@Database(entities = [PasswordEntry::class], version = 1)
abstract class PasswordDatabase : RoomDatabase() {
    abstract fun passwordEntryDAO(): PasswordEntryDao

    companion object {
        @Volatile
        private var INSTANCE: PasswordDatabase? = null

        fun getDatabase(context: Context, pass: String): PasswordDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory =
                    SupportFactory(SQLiteDatabase.getBytes(pass.toCharArray()), null, false)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PasswordDatabase::class.java,
                    "password_database_encrypted"
                ).openHelperFactory(factory).build()


                INSTANCE = instance
                instance
            }
        }
    }
}