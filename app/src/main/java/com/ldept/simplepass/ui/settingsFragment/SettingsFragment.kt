package com.ldept.simplepass.ui.settingsFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.ldept.simplepass.R
import com.ldept.simplepass.databinding.FragmentSettingsBinding
import com.ldept.simplepass.ui.util.CollapsingToolbarStateChangeListener

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSettingsBinding.bind(view)
        val navController = findNavController()

        binding.apply {
            backButton.setOnClickListener {
                navController.navigateUp()
            }

            changePasswordButton.setOnClickListener {
                val action =
                    SettingsFragmentDirections.actionSettingsFragmentToPasswordChangeFragment()
                navController.navigate(action)
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
    }
}