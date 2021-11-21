package com.ldept.simplepass.ui.LoginFragment

import android.database.sqlite.SQLiteException
import android.provider.Settings.Global.getString
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldept.simplepass.R
import com.ldept.simplepass.data.Encryption.PBKDF2
import com.ldept.simplepass.ui.util.SplashScreenAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

class LoginViewModel : ViewModel() {

    private val _password = MutableLiveData<String>()
    val password: LiveData<String>
        get() = _password

    fun onUnlockButtonClick(userPassword: String) {
        checkPassword(userPassword)
    }

    private fun checkPassword(userPassword: String) {
        viewModelScope.launch {
            runCatching {
                PBKDF2.checkPassword(
                    userPassword,
                )
            }
                .onSuccess { password ->
                    onPasswordCorrect(password)
                }
                .onFailure { exception ->
                    when (exception) {
                        is SQLiteException -> onPasswordIncorrect()
                        is FileNotFoundException -> onDatabaseNotFound()
                    }
                }

        }
    }

    private fun onPasswordCorrect(password: String) {
        _password.value = password
    }

    private fun onPasswordIncorrect() {

    }

    private fun onDatabaseNotFound() {

    }
}