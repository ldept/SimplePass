package com.ldept.simplepass.ui.setupFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.ldept.simplepass.R
import com.ldept.simplepass.SimplePassApp
import com.ldept.simplepass.databinding.FragmentSetupBinding
import com.ldept.simplepass.ui.setupFragment.SetupViewModel.SetupEvent.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SetupFragment : Fragment() {

    private lateinit var viewModel: SetupViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSetupBinding.bind(view)

        viewModel = ViewModelProvider(
            this,
            SetupViewModelFactory(SimplePassApp.preferencesRepository)
        ).get(
            SetupViewModel::class.java
        )

        binding.apply {
            nextButton.setOnClickListener {
                // TODO: Check and save password
                // TODO: Save fingerprint preference
                firstPasswordEdittext.clearFocus()
                secondPasswordEdittext.clearFocus()
                val password = firstPasswordEdittext.text.toString()
                val retypedPassword = secondPasswordEdittext.text.toString()
                viewModel.onNextButtonClick(password, retypedPassword)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventFlow.collect { event ->
                    when (event) {
                        is PasswordsDontMatch ->
                            Toast.makeText(
                                activity,
                                getString(R.string.passwords_mismatch),
                                Toast.LENGTH_SHORT
                            ).show()
                        is PasswordTooShort ->
                            Toast.makeText(
                                activity,
                                getString(R.string.passwords_length_requirement),
                                Toast.LENGTH_SHORT
                            ).show()
                        is PasswordOk ->
                            navigateToNextScreen(binding)

                    }
                }
            }

        }
    }

    private fun navigateToNextScreen(binding: FragmentSetupBinding) {
        val password = binding.firstPasswordEdittext.text.toString()
        val action = SetupFragmentDirections.actionSetupFragmentToLoginFragment(password)
        findNavController().navigate(action)
    }
}