package com.ldept.simplepass.data

import androidx.sqlite.db.SimpleSQLiteQuery
import com.ldept.simplepass.data.entities.PasswordEntry
import kotlinx.coroutines.flow.Flow

class PasswordDatabaseRepository(
    private val passwordEntryDao: PasswordEntryDao
) {
    val passwords : Flow<List<PasswordEntry>> =
        passwordEntryDao.getAllPasswords()

    fun searchPasswords(searchQuery : String) : Flow<List<PasswordEntry>> =
        passwordEntryDao.searchPasswords(searchQuery)


    suspend fun insert(passwordEntry: PasswordEntry) =
        passwordEntryDao.insert(passwordEntry)

    suspend fun update(passwordEntry: PasswordEntry) =
        passwordEntryDao.update(passwordEntry)

    suspend fun delete(passwordEntry: PasswordEntry) =
        passwordEntryDao.delete(passwordEntry)

    // Moves every change made to the database from .wal file into the main .db file
    suspend fun checkpoint() =
        passwordEntryDao.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))

    suspend fun changePassword(newPassword: String) =
        passwordEntryDao.changePassword(SimpleSQLiteQuery("pragma rekey='${newPassword}';"))
}