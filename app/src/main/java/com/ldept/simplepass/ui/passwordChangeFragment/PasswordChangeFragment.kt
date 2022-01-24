package com.ldept.simplepass.ui.passwordChangeFragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.ldept.simplepass.R
import com.ldept.simplepass.SimplePassApp
import com.ldept.simplepass.databinding.FragmentPasswordChangeBinding
import com.ldept.simplepass.ui.MainActivity
import com.ldept.simplepass.ui.StartupActivity
import com.ldept.simplepass.ui.util.CollapsingToolbarStateChangeListener
import com.ldept.simplepass.ui.util.SplashScreenAnimation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class PasswordChangeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_change, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPasswordChangeBinding.bind(view)
        val repository = (activity as MainActivity).repository
        val viewModel = ViewModelProvider(
            this,
            PasswordChangeViewModelFactory(repository, SimplePassApp.preferencesRepository)
        )
            .get(PasswordChangeViewModel::class.java)

        val splashScreen = binding.splashScreenLayout.splashScreen
        val fragmentContent = binding.content

        binding.apply {
            backButton.setOnClickListener {
                findNavController().navigateUp()
            }
            changePasswordButton.setOnClickListener {
                val newPassword = firstPasswordEdittext.text.toString()
                val retypedPassword = secondPasswordEdittext.text.toString()
                viewModel.onChangePassword(newPassword, retypedPassword)
            }

            // Change AppBar text visibility if scrollview is scrolled
            appBarLayout.addOnOffsetChangedListener(object :
                CollapsingToolbarStateChangeListener() {
                override fun onStateChanged(appBarLayout: AppBarLayout?, currentState: State) {
                    if (currentState == State.COLLAPSED) {
                        titleTextView.visibility = View.INVISIBLE
                        titleTextViewToolbar.visibility = View.VISIBLE
                    } else {
                        titleTextView.visibility = View.VISIBLE
                        titleTextViewToolbar.visibility = View.INVISIBLE
                    }

                }
            })
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsFlow.collect { event ->
                    when (event) {
                        is PasswordChangeViewModel.PasswordChangeEvent.PasswordChanging ->
                            SplashScreenAnimation.crossfade(fragmentContent, splashScreen)
                        is PasswordChangeViewModel.PasswordChangeEvent.PasswordTooShort ->
                            Toast.makeText(
                                activity,
                                getString(R.string.passwords_length_requirement),
                                Toast.LENGTH_SHORT
                            ).show()
                        is PasswordChangeViewModel.PasswordChangeEvent.PasswordsDontMatch ->
                            Toast.makeText(
                                activity,
                                getString(R.string.passwords_mismatch),
                                Toast.LENGTH_SHORT
                            ).show()
                        is PasswordChangeViewModel.PasswordChangeEvent.PasswordChanged -> {
                            Toast.makeText(
                                activity,
                                getString(R.string.password_change_success),
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(activity, StartupActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }
                    }
                }
            }
        }
    }
}