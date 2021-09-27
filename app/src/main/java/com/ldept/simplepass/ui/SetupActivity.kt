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
import com.ldept.simplepass.databinding.ActivitySetupBinding
import com.ldept.simplepass.util.PBKDF2
import com.ldept.simplepass.util.SplashScreenAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Compiler.enable




class SetupActivity : AppCompatActivity(), BiometricAuthenticationListener {

    private lateinit var fingerprintSwitch : SwitchCompat
    private lateinit var binding : ActivitySetupBinding
    private var isFingerprintUnlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE)

        binding = ActivitySetupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val nextButton : Button = binding.nextButton
        val contentView : View = binding.loginSetupContent
        val splashScreenView : View = binding.splashScreenLayout.splashScreen
        fingerprintSwitch = binding.fingerprintSwitch
        val fingerprintGroup : Group = binding.fingerprintGroup

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
            val firstPassEditText : EditText = binding.firstPasswordEdittext
            val secondPassEditText : EditText = binding.secondPasswordEdittext
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
                            changePassword = false
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