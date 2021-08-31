package com.ldept.simplepass.ui

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.biometric.BiometricPrompt
import com.ldept.simplepass.R
import com.ldept.simplepass.biometrics.BiometricAuthentication
import com.ldept.simplepass.biometrics.BiometricAuthenticationListener
import com.ldept.simplepass.util.PBKDF2
import com.ldept.simplepass.util.SplashScreenAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PasswordSetupFragment : Fragment(), BiometricAuthenticationListener {

    private var isFingerprintUnlocked = false
    private lateinit var fingerprintSwitch : SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_password_setup, container, false)

        val nextButton : Button = view.findViewById(R.id.next_button)
        val contentView : View = view.findViewById(R.id.login_setup_content)
        val splashScreenView : View = view.findViewById(R.id.splash_screen)
        fingerprintSwitch = view.findViewById(R.id.fingerprint_switch)
        val fingerprintText : TextView = view.findViewById(R.id.fingerprint_enable_text)
        val fingerprintUnderText : TextView = view.findViewById(R.id.fingerprint_enable_under_text)

        this.context?.let {
            BiometricAuthentication.showBiometricLoginOption(it, fingerprintSwitch)
            BiometricAuthentication.showBiometricLoginOption(it, fingerprintText)
            BiometricAuthentication.showBiometricLoginOption(it, fingerprintUnderText)
        }
        fingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked && !isFingerprintUnlocked)
                BiometricAuthentication.showBiometricAuthentication(
                    this.activity as AppCompatActivity,
                    this
                )
            else
                isFingerprintUnlocked = false
        }

        nextButton.setOnClickListener {
            val firstPassEditText : EditText = view.findViewById(R.id.first_password_edittext)
            val secondPassEditText : EditText = view.findViewById(R.id.second_password_edittext)
            val firstPassText = firstPassEditText.text.toString()
            val secondPassText = secondPassEditText.text.toString()
            if(firstPassText.length >= 10 && firstPassText == secondPassText){

                SplashScreenAnimation.crossfade(contentView, splashScreenView)
                activity?.let { activity ->
                    val sharedPrefs = activity.getSharedPreferences(activity.packageName, MODE_PRIVATE)
                    sharedPrefs.edit().putBoolean("app-already-set-up", true).apply()
                    sharedPrefs.edit().putBoolean("fingerprint-unlock", isFingerprintUnlocked).apply()
                    CoroutineScope(Dispatchers.IO).launch {
                        PBKDF2.checkOrCreatePassword(
                            firstPassText,
                            activity as AppCompatActivity,
                            sharedPrefs,
                            createNewPassword = false
                        ) {
                            Toast.makeText(activity, "Invalid Password", Toast.LENGTH_LONG).show()
                            SplashScreenAnimation.crossfade(splashScreenView, contentView)
                        }
                    }
                }

            } else {
                Toast.makeText(activity, getString(R.string.password_setup_mismatch), Toast.LENGTH_LONG).show()
            }

        }

        return view
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



}