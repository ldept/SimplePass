package com.ldept.simplepass.ui.passwordListFragment

import androidx.lifecycle.*
import com.ldept.simplepass.data.entities.PasswordEntry
import com.ldept.simplepass.data.PasswordDatabaseRepository
import com.ldept.simplepass.data.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PasswordListViewModel(
    private val repository: PasswordDatabaseRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val passwords: LiveData<List<PasswordEntry>> = repository.passwords.asLiveData()

    init {
        setIsFirstLaunchToFalse()
    }

    fun searchPasswords(searchQuery: String): LiveData<List<PasswordEntry>> =
        repository.searchPasswords(searchQuery).asLiveData()

    private fun setIsFirstLaunchToFalse(){
        viewModelScope.launch {
            if(preferencesRepository.userPreferencesFlow.first().isFirstLaunch)
                preferencesRepository.setIsFirstLaunch(false)
        }
    }
    fun setDbFilePath(dbFilePath: String) {
        viewModelScope.launch {
            if(preferencesRepository.userPreferencesFlow.first().dbFilePath.isEmpty())
                preferencesRepository.setDbFilePath(dbFilePath)
        }
    }

}

class PasswordListViewModelFactory(
    private val repository: PasswordDatabaseRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PasswordListViewModel(repository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}