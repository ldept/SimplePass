package com.ldept.simplepass.data

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.ldept.simplepass.data.entities.PasswordEntry
import kotlinx.coroutines.flow.Flow


@Dao
interface PasswordEntryDao {
    @Insert
    suspend fun insert(passwordEntry : PasswordEntry)

    @Update
    suspend fun update(passwordEntry: PasswordEntry)

    @Delete
    suspend fun delete(passwordEntry : PasswordEntry)

    @Query("SELECT * FROM password_entry_table ORDER BY name ASC, LENGTH(name)")
    fun getAllPasswords() : Flow<List<PasswordEntry>>

    @Query("SELECT * FROM  password_entry_table WHERE name LIKE :searchQuery OR login LIKE :searchQuery ORDER BY name ASC, LENGTH(name)")
    fun searchPasswords(searchQuery : String) : Flow<List<PasswordEntry>>

    @RawQuery
    suspend fun checkpoint(sqliteQuery : SupportSQLiteQuery) : Int

    @RawQuery
    suspend fun changePassword(sqliteQuery : SupportSQLiteQuery) : Int

}