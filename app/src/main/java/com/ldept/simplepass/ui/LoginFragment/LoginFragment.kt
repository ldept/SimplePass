package com.ldept.simplepass.ui.LoginFragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ldept.simplepass.R
import com.ldept.simplepass.databinding.FragmentLoginBinding
import com.ldept.simplepass.databinding.SplashScreenBinding
import com.ldept.simplepass.ui.MainActivity
import com.ldept.simplepass.ui.StartupActivity
import com.ldept.simplepass.ui.util.SplashScreenAnimation

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentLoginBinding.bind(view)
        val viewModel: LoginViewModel by viewModels()


        val splashScreenBinding = SplashScreenBinding.inflate(layoutInflater)

        binding.apply{
            unlockButton.setOnClickListener {
                val splashScreen = splashScreenBinding.splashScreen
                val loginScreen = binding.loginContent
                val userPassword = loginPasswordEditText.text.toString()
                SplashScreenAnimation.crossfade(loginScreen,splashScreen)
                viewModel.onUnlockButtonClick(userPassword)
            }
        }

        viewModel.password.observe(this, Observer { password ->
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra(StartupActivity.EXTRA_PASSWORD, password)
            startActivity(intent)
            activity?.finish()
        })
    }

}