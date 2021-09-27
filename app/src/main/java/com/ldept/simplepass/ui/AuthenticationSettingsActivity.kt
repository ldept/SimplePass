package com.ldept.simplepass.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.ldept.simplepass.R
import com.ldept.simplepass.databinding.ActivityAuthenticationSettingsBinding
import com.ldept.simplepass.util.PBKDF2
import com.ldept.simplepass.util.SplashScreenAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthenticationSettingsActivity : AppCompatActivity() {
    companion object {
        const val NEW_PASSWORD_EXTRA = "package com.ldept.simplepass.EXTRA_NEW_PASSWORD"
    }

    private lateinit var binding : ActivityAuthenticationSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE)

        binding = ActivityAuthenticationSettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        title = getString(R.string.change_password)


        val newPasswordEditText : EditText = binding.firstPasswordEdittext
        val retypedPasswordEditText : EditText = binding.secondPasswordEdittext
        val contentView : View = binding.content
        val splashScreenView : View = binding.splashScreenLayout.splashScreen
        val changePasswordButton : Button = binding.changePasswordButton

        changePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()
            val retypedPassword = retypedPasswordEditText.text.toString()

            if( newPassword == retypedPassword
                && newPassword.length >= 10){
                val sharedPrefs = getSharedPreferences(packageName, MODE_PRIVATE)
                val activity = this
                SplashScreenAnimation.crossfade(contentView, splashScreenView)
                CoroutineScope(Dispatchers.IO).launch {
                    PBKDF2.checkOrCreatePassword(
                        newPassword,
                        activity,
                        sharedPrefs,
                        changePassword = true
                    ) {
                        Toast.makeText(activity, getString(R.string.invalid_password), Toast.LENGTH_LONG).show()
                        SplashScreenAnimation.crossfade(splashScreenView, contentView)
                    }
                }

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(NEW_PASSWORD_EXTRA, newPassword)
                startActivity(intent)
            }
            else {
                if(newPassword != retypedPassword)
                    Toast.makeText(this, getString(R.string.new_passwords_mismatch), Toast.LENGTH_LONG).show()
                if(newPassword.length < 10)
                    Toast.makeText(this, getString(R.string.new_passwords_len), Toast.LENGTH_LONG).show()
            }
        }
    }
}