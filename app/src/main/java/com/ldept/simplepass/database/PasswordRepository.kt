package com.ldept.simplepass.database

import androidx.annotation.WorkerThread
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow


class PasswordRepository(private val passwordEntryDao : PasswordEntryDAO) {
    val allPasswords : Flow<List<PasswordEntry>> = passwordEntryDao.getAllPasswords()

    @WorkerThread
    suspend fun insert(passwordEntry : PasswordEntry){
        passwordEntryDao.insertAllPasswords(passwordEntry)
    }

    @WorkerThread
    suspend fun update(passwordEntry : PasswordEntry){
        passwordEntryDao.updatePassword(passwordEntry)
    }

    fun searchPasswords(searchQuery : String) : Flow<List<PasswordEntry>>{
        return passwordEntryDao.searchPasswords(searchQuery)
    }

    @WorkerThread
    suspend fun delete(passwordEntry : PasswordEntry){
        passwordEntryDao.deletePassword(passwordEntry)
    }

    @WorkerThread
    suspend fun checkpoint(sqliteQuery : SimpleSQLiteQuery) {
        passwordEntryDao.checkpoint(sqliteQuery)
    }

    @WorkerThread
    suspend fun changePassword(sqliteQuery : SimpleSQLiteQuery) {
        passwordEntryDao.changePassword(sqliteQuery)
    }

}