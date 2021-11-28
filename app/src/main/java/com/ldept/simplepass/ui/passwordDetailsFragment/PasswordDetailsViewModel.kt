package com.ldept.simplepass.ui.passwordDetailsFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ldept.simplepass.data.entities.PasswordEntry
import com.ldept.simplepass.data.PasswordDatabaseRepository
import kotlinx.coroutines.launch

class PasswordDetailsViewModel(
    private val repository: PasswordDatabaseRepository
) : ViewModel() {

    fun insert(passwordEntry: PasswordEntry) = viewModelScope.launch {
        repository.insert(passwordEntry)
    }

    fun update(passwordEntry: PasswordEntry) = viewModelScope.launch {
        repository.update(passwordEntry)
    }

    fun delete(passwordEntry: PasswordEntry) = viewModelScope.launch {
        repository.delete(passwordEntry)
    }
}

class PasswordDetailsViewModelFactory(
    private val repository: PasswordDatabaseRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(PasswordDetailsViewModel::class.java))
            return PasswordDetailsViewModel(repository) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }

}