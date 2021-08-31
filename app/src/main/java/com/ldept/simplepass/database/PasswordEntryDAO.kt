package com.ldept.simplepass.database

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordEntryDAO {
    @Insert
    suspend fun insertAllPasswords(vararg passwordEntries : PasswordEntry)

    @Update
    suspend fun updatePassword(passwordEntry: PasswordEntry)

    @Delete
    suspend fun deletePassword(passwordEntry : PasswordEntry)

    @Query("SELECT * FROM password_entry_table ORDER BY LENGTH(name), name ASC ")
    fun getAllPasswords() : Flow<List<PasswordEntry>>

    @Query("SELECT * FROM  password_entry_table WHERE name LIKE :searchQuery OR login LIKE :searchQuery ORDER BY LENGTH(name), name ASC")
    fun searchPasswords(searchQuery : String) : Flow<List<PasswordEntry>>

    @RawQuery
    suspend fun checkpoint(sqliteQuery : SupportSQLiteQuery) : Int

    @RawQuery
    suspend fun changePassword(sqliteQuery : SupportSQLiteQuery) : Int
}