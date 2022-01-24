package com.ldept.simplepass.ui.loginFragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ldept.simplepass.R
import com.ldept.simplepass.SimplePassApp
import com.ldept.simplepass.databinding.FragmentLoginBinding
import com.ldept.simplepass.ui.loginFragment.LoginViewModel.LoginEvent.*
import com.ldept.simplepass.ui.MainActivity
import com.ldept.simplepass.ui.StartupActivity
import com.ldept.simplepass.ui.util.SplashScreenAnimation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val args: LoginFragmentArgs by navArgs<LoginFragmentArgs>()

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
        val viewModel: LoginViewModel =
            ViewModelProvider(this, LoginViewModelFactory(SimplePassApp.preferencesRepository, args.setupPassword))
                .get(
                    LoginViewModel::class.java
                )
        val splashScreen = binding.splashScreenLayout.splashScreen
        val loginScreen = binding.loginContent

        args.setupPassword?.let {
            SplashScreenAnimation.crossfade(loginScreen, splashScreen)
            viewModel.onUnlockButtonClick(it)
        }

        binding.apply {
            unlockButton.setOnClickListener {
                val userPassword = loginPasswordEditText.text.toString()
                SplashScreenAnimation.crossfade(loginScreen, splashScreen)
                viewModel.onUnlockButtonClick(userPassword)
            }
        }

        viewModel.password.observe(viewLifecycleOwner) { password ->
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra(StartupActivity.EXTRA_PASSWORD, password)
            startActivity(intent)
            activity?.finish()
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsFlow.collect { event ->
                    when (event) {
                        is FirstLaunch ->
                            navigateToSetupScreen()
                        is ShowPasswordIncorrectToast ->
                            Toast.makeText(
                                activity,
                                getString(R.string.invalid_password),
                                Toast.LENGTH_SHORT
                            ).show()
                        is ShowDatabaseNotFoundToast ->
                            Toast.makeText(
                                activity,
                                getString(R.string.database_file_not_found),
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                    SplashScreenAnimation.crossfade(splashScreen, loginScreen)
                }
            }
        }
    }

    private fun navigateToSetupScreen(){
        val action = LoginFragmentDirections.actionLoginFragmentToSetupFragment()
        findNavController().navigate(action)
    }

}