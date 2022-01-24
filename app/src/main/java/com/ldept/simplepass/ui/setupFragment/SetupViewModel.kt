package com.ldept.simplepass.ui.setupFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ldept.simplepass.data.PreferencesRepository
import com.ldept.simplepass.ui.setupFragment.SetupViewModel.SetupEvent.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SetupViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    sealed class SetupEvent {
        object PasswordsDontMatch : SetupEvent()
        object PasswordTooShort : SetupEvent()
        object PasswordOk : SetupEvent()
    }

    private val eventChannel = Channel<SetupEvent>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private val userPreferencesFlow = preferencesRepository.userPreferencesFlow

    fun onNextButtonClick(password: String, retypedPassword: String) {
        if (checkPasswordRequirements(password, retypedPassword))
            viewModelScope.launch { eventChannel.send(PasswordOk) }
    }


    private fun checkPasswordRequirements(password: String, retypedPassword: String): Boolean {
        if (password != retypedPassword) {
            viewModelScope.launch { eventChannel.send(PasswordsDontMatch) }
            return false
        }
        if (password.length <= 10) {
            viewModelScope.launch { eventChannel.send(PasswordTooShort) }
            return false
        }
        return true
    }

}

class SetupViewModelFactory(
    private val preferencesRepository: PreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SetupViewModel::class.java))
            return SetupViewModel(preferencesRepository) as T
        else
            throw IllegalArgumentException("Unknown ViewModel class")
    }

}