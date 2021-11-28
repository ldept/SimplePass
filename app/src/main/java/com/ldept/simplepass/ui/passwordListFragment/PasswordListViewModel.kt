package com.ldept.simplepass.ui.passwordListFragment

import androidx.lifecycle.*
import com.ldept.simplepass.data.entities.PasswordEntry
import com.ldept.simplepass.data.PasswordDatabaseRepository
import kotlinx.coroutines.launch

class PasswordListViewModel(
    private val repository: PasswordDatabaseRepository
) : ViewModel() {

    val passwords: LiveData<List<PasswordEntry>> = repository.passwords.asLiveData()



    fun searchPasswords(searchQuery: String): LiveData<List<PasswordEntry>> =
        repository.searchPasswords(searchQuery).asLiveData()

    fun checkpoint() = viewModelScope.launch {
        repository.checkpoint()
    }

    fun changePassword(newPassword: String) = viewModelScope.launch {
        repository.changePassword(newPassword)
    }

}

class PasswordListViewModelFactory(
    private val repository: PasswordDatabaseRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PasswordListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}