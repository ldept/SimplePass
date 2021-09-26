package com.ldept.simplepass.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.biometric.BiometricPrompt
import androidx.constraintlayout.widget.Group
import com.ldept.simplepass.R
import com.ldept.simplepass.biometrics.BiometricAuthentication
import com.ldept.simplepass.biometrics.BiometricAuthenticationListener
import com.ldept.simplepass.util.PBKDF2
import com.ldept.simplepass.util.SplashScreenAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Compiler.enable




class SetupActivity : AppCompatActivity(), BiometricAuthenticationListener {

    private lateinit var fingerprintSwitch : SwitchCompat
    private var isFingerprintUnlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_setup)


        val nextButton : Button = findViewById(R.id.next_button)
        val contentView : View = findViewById(R.id.login_setup_content)
        val splashScreenView : View = findViewById(R.id.splash_screen)
        fingerprintSwitch = findViewById(R.id.fingerprint_switch)
        val fingerprintGroup : Group = findViewById(R.id.fingerprint_group)

        BiometricAuthentication.showBiometricLoginOption(this, fingerprintGroup)

        fingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked && !isFingerprintUnlocked)
                BiometricAuthentication.showBiometricAuthentication(
                    this,
                    this
                )

            else if(!isChecked)
                isFingerprintUnlocked = false
        }

        nextButton.setOnClickListener {
            val firstPassEditText : EditText = findViewById(R.id.first_password_edittext)
            val secondPassEditText : EditText = findViewById(R.id.second_password_edittext)
            val firstPassText = firstPassEditText.text.toString()
            val secondPassText = secondPassEditText.text.toString()
            if(firstPassText.length >= 10 && firstPassText == secondPassText){

                SplashScreenAnimation.crossfade(contentView, splashScreenView)

                val sharedPrefs = getSharedPreferences(packageName, MODE_PRIVATE)
                sharedPrefs.edit().putBoolean("app-already-set-up", true).apply()
                sharedPrefs.edit().putBoolean("fingerprint-unlock", isFingerprintUnlocked).apply()
                this.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        PBKDF2.checkOrCreatePassword(
                            firstPassText,
                            it,
                            sharedPrefs,
                            createNewPassword = false
                        ) {
                            Toast.makeText(it, "Invalid Password", Toast.LENGTH_LONG).show()
                            SplashScreenAnimation.crossfade(splashScreenView, contentView)
                        }
                    }
                }

            } else {
                Toast.makeText(this, getString(R.string.password_setup_mismatch), Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        isFingerprintUnlocked = true
        fingerprintSwitch.isChecked = true
    }

    override fun onAuthenticationFailed() {
        isFingerprintUnlocked = false
        fingerprintSwitch.isChecked = false
    }

    override fun onAuthenticationError(errorCode: Int, errString: String) {
        isFingerprintUnlocked = false
        fingerprintSwitch.isChecked = false
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean("isFingerprintUnlocked", isFingerprintUnlocked)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        isFingerprintUnlocked = savedInstanceState.getBoolean("isFingerprintUnlocked")
        super.onRestoreInstanceState(savedInstanceState)
    }

}