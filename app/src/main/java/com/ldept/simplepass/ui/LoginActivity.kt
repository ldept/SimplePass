package com.ldept.simplepass.ui

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import com.ldept.simplepass.R
import com.ldept.simplepass.biometrics.BiometricAuthentication
import com.ldept.simplepass.biometrics.BiometricAuthenticationListener
import com.ldept.simplepass.util.PBKDF2
import com.ldept.simplepass.util.SplashScreenAnimation
import kotlinx.coroutines.*
import net.sqlcipher.database.SQLiteDatabase

class LoginActivity : AppCompatActivity(), BiometricAuthenticationListener {

    companion object{
        const val EXTRA_PASSWORD = "package com.ldept.simplepass.EXTRA_PASSWORD"
    }

    private lateinit var loginContentView : View
    private lateinit var splashScreenView : View
    private lateinit var userPassword : String
    private lateinit var loginActivity : AppCompatActivity
    private lateinit var sharedPrefs : SharedPreferences
    private lateinit var editText : EditText

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        checkPassword()
    }

    override fun onAuthenticationFailed() {
        Toast.makeText(this, getString(R.string.authentication_failed), Toast.LENGTH_LONG ).show()
    }

    override fun onAuthenticationError(errorCode: Int, errString: String) {
        Toast.makeText(this,
            "${getString(R.string.authentication_error)}: $errString", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginContentView = findViewById(R.id.login_content)
        splashScreenView = findViewById(R.id.splash_screen)
        sharedPrefs = getSharedPreferences(packageName, MODE_PRIVATE)

        if(!sharedPrefs.getBoolean("app-already-set-up",false)){
            val intent = Intent(this, SetupActivity::class.java)
            startActivity(intent)
            finish()
        }

        SQLiteDatabase.loadLibs(this)

        val unlockButton : Button = findViewById(R.id.unlock_button)
        editText = findViewById(R.id.login_password_editText)

        unlockButton.setOnClickListener{
            userPassword = editText.text.toString()
            loginActivity = this
            val isFingerprintUnlocked = sharedPrefs.getBoolean("fingerprint-unlock",false)
            if(isFingerprintUnlocked)
                BiometricAuthentication.showBiometricAuthentication(
                    this,
                    this
                )
            else {
                checkPassword()
            }

        }

    }

    private fun checkPassword(){
        if (userPassword.isNotEmpty()) {
            SplashScreenAnimation.crossfade(loginContentView, splashScreenView)

            CoroutineScope(Dispatchers.IO).launch {
                PBKDF2.checkOrCreatePassword(userPassword, loginActivity, sharedPrefs, createNewPassword = false) {
                    Toast.makeText(loginActivity, getString(R.string.invalid_password), Toast.LENGTH_LONG).show()
                    editText.text.clear()
                    SplashScreenAnimation.crossfade(splashScreenView, loginContentView)
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.empty_password), Toast.LENGTH_SHORT).show()
        }
    }



}