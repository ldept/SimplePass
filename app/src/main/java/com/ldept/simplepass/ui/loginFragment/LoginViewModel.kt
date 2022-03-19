package com.ldept.simplepass.ui.loginFragment


import android.util.Log
import androidx.lifecycle.*
import com.ldept.simplepass.data.PreferencesRepository
import com.ldept.simplepass.data.encryption.PBKDF2
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import net.sqlcipher.database.SQLiteException
import java.io.FileNotFoundException

class LoginViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val setupPassword: String?
) : ViewModel() {

    sealed class LoginEvent {
        object ShowPasswordIncorrectToast : LoginEvent()
        object ShowPasswordEmptyToast : LoginEvent()
        object ShowDatabaseNotFoundToast : LoginEvent()
        object FirstLaunch : LoginEvent()
    }

    private val _password = MutableLiveData<String>()
    val password: LiveData<String>
        get() = _password

    private val eventChannel = Channel<LoginEvent>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    private val userPreferencesFlow = preferencesRepository.userPreferencesFlow

    init {
        getIsFirstLaunch()
    }

    fun onUnlockButtonClick(userPassword: String) {
        if (userPassword.isNotEmpty())
            checkPassword(userPassword)
        else
            viewModelScope.launch {
                eventChannel.send(LoginEvent.ShowPasswordEmptyToast)
            }
    }

    private fun getIsFirstLaunch() {
        viewModelScope.launch {
            val isFirstLaunch: Boolean = userPreferencesFlow.first().isFirstLaunch
            if (isFirstLaunch && setupPassword == null)
                eventChannel.send(LoginEvent.FirstLaunch)
        }
    }

    private fun checkPassword(userPassword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // TODO: This doesn't seem to be the correct way to do this
            val prefs = userPreferencesFlow.first()
            val userSalt = prefs.pbkdf2Salt
            val dbFilePath = prefs.dbFilePath

            runCatching {

                PBKDF2.checkDatabasePassword(
                    userPassword,
                    userSalt,
                    dbFilePath
                )
            }
                .onSuccess { (password, salt) ->
                    onPasswordCorrect(password, salt)
                }
                .onFailure { exception ->
                    val message = exception.message.toString()
                    when (exception) {
                        is SQLiteException -> onPasswordIncorrect(message)
                        is FileNotFoundException -> onDatabaseNotFound(message)
                        else -> Log.d("Login Failure", exception.toString())
                    }
                }

        }
    }

    private suspend fun onPasswordCorrect(password: String, salt: String) {
        preferencesRepository.setPBKDF2Salt(salt)
        _password.postValue(password)
    }

    private suspend fun onPasswordIncorrect(errorMessage: String) {
        eventChannel.send(LoginEvent.ShowPasswordIncorrectToast)
        Log.d("LoginViewModel", errorMessage)
    }

    private suspend fun onDatabaseNotFound(errorMessage: String) {
        eventChannel.send(LoginEvent.ShowDatabaseNotFoundToast)
        Log.d("LoginViewModel", errorMessage)
    }
}


class LoginViewModelFactory(
    private val preferencesRepository: PreferencesRepository,
    private val setupPassword: String?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java))
            return LoginViewModel(preferencesRepository, setupPassword) as T
        else
            throw IllegalArgumentException("Unknown ViewModel class")
    }
}