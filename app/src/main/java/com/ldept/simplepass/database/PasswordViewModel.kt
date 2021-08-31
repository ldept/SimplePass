package com.ldept.simplepass.database

import androidx.lifecycle.*
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class PasswordViewModel(private val repository: PasswordRepository) : ViewModel() {
    val allPasswords : LiveData<List<PasswordEntry>> = repository.allPasswords.asLiveData()

    fun insert(passwordEntry: PasswordEntry) = viewModelScope.launch {
        repository.insert(passwordEntry)
    }

    fun update(passwordEntry: PasswordEntry) = viewModelScope.launch {
        repository.update(passwordEntry)
    }

    fun delete(passwordEntry: PasswordEntry) = viewModelScope.launch {
        repository.delete(passwordEntry)
    }

    fun searchPasswords(searchQuery : String) : LiveData<List<PasswordEntry>> {
        return repository.searchPasswords(searchQuery).asLiveData()
    }

    fun checkpoint(sqLiteQuery: SimpleSQLiteQuery) = viewModelScope.launch {
        repository.checkpoint(sqLiteQuery)
    }
    fun changePassword(sqLiteQuery: SimpleSQLiteQuery) = viewModelScope.launch {
        repository.changePassword(sqLiteQuery)
    }
}

class PasswordViewModelFactory(private val repository: PasswordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(PasswordViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return  PasswordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}