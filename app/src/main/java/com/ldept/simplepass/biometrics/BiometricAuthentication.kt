package com.ldept.simplepass.biometrics

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ldept.simplepass.R
import javax.crypto.Cipher


interface BiometricAuthenticationListener {
    fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult)
    fun onAuthenticationError(errorCode: Int, errString: String)
    fun onAuthenticationFailed()
}

object BiometricAuthentication {


    private fun isCapableOfBiometricAuthentication(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG)
    }

    fun showBiometricLoginOption(context : Context, view : View) {
        view.visibility =
            if (isCapableOfBiometricAuthentication(context) == BIOMETRIC_SUCCESS)
                View.VISIBLE
            else
                View.GONE
    }

    private fun initBiometricPrompt(
        activity: AppCompatActivity,
        listener: BiometricAuthenticationListener
    ): BiometricPrompt {

        val executor = ContextCompat.getMainExecutor(activity)
        return BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    listener.onAuthenticationError(errorCode, errString.toString())
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    listener.onAuthenticationSucceeded(result)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    listener.onAuthenticationFailed()
                }
            })
    }
    fun showBiometricAuthentication(
        activity: AppCompatActivity,
        listener: BiometricAuthenticationListener
    ){
        val biometricPrompt = initBiometricPrompt(activity, listener)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("SimplePass")
            .setSubtitle(activity.getString(R.string.biometric_prompt_message))
            .setNegativeButtonText(activity.getString(R.string.cancel))
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}