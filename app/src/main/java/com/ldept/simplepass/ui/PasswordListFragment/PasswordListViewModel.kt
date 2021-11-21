package com.ldept.simplepass.ui.PasswordListFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ldept.simplepass.data.Entities.PasswordEntry
import com.ldept.simplepass.data.PasswordDatabaseRepository
import kotlinx.coroutines.launch

class PasswordListViewModel(
    private val repository : PasswordDatabaseRepository
) : ViewModel() {

    val passwords : LiveData<List<PasswordEntry>> = repository.passwords.asLiveData()

    fun insert(passwordEntry: PasswordEntry) = viewModelScope.launch {
        repository.insert(passwordEntry)
    }

    fun update(passwordEntry: PasswordEntry) = viewModelScope.launch {
        repository.update(passwordEntry)
    }

    fun delete(passwordEntry: PasswordEntry) = viewModelScope.launch {
        repository.delete(passwordEntry)
    }

    fun searchPasswords(searchQuery : String) : LiveData<List<PasswordEntry>> =
        repository.searchPasswords(searchQuery).asLiveData()

    fun checkpoint() = viewModelScope.launch {
        repository.checkpoint()
    }

    fun changePassword(newPassword: String) = viewModelScope.launch {
        repository.changePassword(newPassword)
    }

}