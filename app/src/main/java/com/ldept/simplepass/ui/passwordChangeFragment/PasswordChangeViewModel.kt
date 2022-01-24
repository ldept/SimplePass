package com.ldept.simplepass.ui.passwordChangeFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ldept.simplepass.data.PasswordDatabaseRepository
import com.ldept.simplepass.data.PreferencesRepository
import com.ldept.simplepass.data.UserPreferences
import com.ldept.simplepass.data.encryption.PBKDF2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PasswordChangeViewModel(
    private val repository: PasswordDatabaseRepository,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    sealed class PasswordChangeEvent {
        object PasswordChanged : PasswordChangeEvent()
        object PasswordTooShort : PasswordChangeEvent()
        object PasswordsDontMatch : PasswordChangeEvent()
        object PasswordChanging : PasswordChangeEvent()
    }

    private val userPreferencesFlow = preferencesRepository.userPreferencesFlow

    private val eventChannel = Channel<PasswordChangeEvent>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    fun onChangePassword(newPassword: String, retypedPassword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (checkPasswordRequirements(newPassword, retypedPassword)) {
                eventChannel.send(PasswordChangeEvent.PasswordChanging)
                changePassword(newPassword)
            }
        }
    }

    private suspend fun checkPasswordRequirements(
        password: String,
        retypedPassword: String
    ): Boolean {
        if (password.length < 10) {
            eventChannel.send(PasswordChangeEvent.PasswordTooShort)
            return false
        }
        if (password != retypedPassword) {
            eventChannel.send(PasswordChangeEvent.PasswordsDontMatch)
            return false
        }
        return true
    }

    private suspend fun changePassword(newPassword: String) {
        // TODO: Fetch userSalt from prefs
        val userSalt: String = userPreferencesFlow.first().pbkdf2Salt
        val password = PBKDF2.getNewPassword(newPassword, userSalt)
        repository.changePassword(password)
        eventChannel.send(PasswordChangeEvent.PasswordChanged)
    }
}

class PasswordChangeViewModelFactory(
    private val repository: PasswordDatabaseRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(PasswordChangeViewModel::class.java)) {
            return PasswordChangeViewModel(repository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}