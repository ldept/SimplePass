package com.ldept.simplepass.ui.loginFragment


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ldept.simplepass.data.encryption.PBKDF2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteException
import java.io.FileNotFoundException

class LoginViewModel : ViewModel() {

    sealed class LoginEvent {
        object ShowPasswordIncorrectToast : LoginEvent()
        object ShowDatabaseNotFoundToast  : LoginEvent()
    }
    private val _password = MutableLiveData<String>()
    val password: LiveData<String>
        get() = _password

    private val eventChannel = Channel<LoginEvent>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    fun onUnlockButtonClick(userPassword: String) {
        checkPassword(userPassword)
    }

    private fun checkPassword(userPassword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                PBKDF2.checkPassword(
                    userPassword,
                )
            }
                .onSuccess { password ->
                    onPasswordCorrect(password)
                }
                .onFailure { exception ->
                    val message = exception.message.toString()
                    when (exception) {
                        is SQLiteException -> onPasswordIncorrect(message)
                        is FileNotFoundException -> onDatabaseNotFound(message)
                        else -> Log.i("Login Failure", exception.toString())
                    }
                }

        }
    }

    private fun onPasswordCorrect(password: String) {
        _password.postValue(password)
    }

    private suspend fun onPasswordIncorrect(errorMessage: String) {
        eventChannel.send(LoginEvent.ShowPasswordIncorrectToast)
        Log.e("LoginViewModel", errorMessage)
    }

    private suspend fun onDatabaseNotFound(errorMessage: String) {
        eventChannel.send(LoginEvent.ShowDatabaseNotFoundToast)
        Log.e("LoginViewModel", errorMessage)
    }
}