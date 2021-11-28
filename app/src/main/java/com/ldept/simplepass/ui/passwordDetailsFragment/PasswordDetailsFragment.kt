package com.ldept.simplepass.ui.passwordDetailsFragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.ldept.simplepass.R
import com.ldept.simplepass.data.entities.PasswordEntry
import com.ldept.simplepass.databinding.FragmentPasswordDetailsBinding
import com.ldept.simplepass.ui.MainActivity
import com.ldept.simplepass.ui.util.CollapsingToolbarStateChangeListener

class PasswordDetailsFragment : Fragment() {

    private val args: PasswordDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val passwordEntryArg: PasswordEntry? = args.passwordEntry

        val binding = FragmentPasswordDetailsBinding.bind(view)
        val repository = (activity as MainActivity).repository
        val viewModel = ViewModelProvider(this, PasswordDetailsViewModelFactory(repository))
            .get(PasswordDetailsViewModel::class.java)

        binding.apply {

            passwordEntryArg?.let {
                nameAddEditText.setText(it.name)
                loginAddEditText.setText(it.login)
                passwordAddEditText.setText(it.password)
                emailAddEditText.setText(it.email)

                copyEmailButton.visibility = View.VISIBLE
                copyLoginButton.visibility = View.VISIBLE
                copyPasswordButton.visibility = View.VISIBLE

                copyPasswordButton.setOnClickListener {
                    val password = passwordAddEditText.text.toString()
                    copyToClipboard(password)
                }
                copyLoginButton.setOnClickListener {
                    val login = loginAddEditText.text.toString()
                    copyToClipboard(login)
                }
                copyEmailButton.setOnClickListener {
                    val email = emailAddEditText.text.toString()
                    copyToClipboard(email)
                }
            }

            passwordAddSaveButton.setOnClickListener {
                val passwordEntry = PasswordEntry(
                    id = passwordEntryArg?.id ?: 0,
                    name = nameAddEditText.text.toString(),
                    login = loginAddEditText.text.toString(),
                    email = emailAddEditText.text.toString(),
                    password = passwordAddEditText.text.toString()
                )

                if (passwordEntryArg == null)
                    viewModel.insert(passwordEntry)
                else viewModel.update(passwordEntry)

                val action = PasswordDetailsFragmentDirections
                    .actionPasswordDetailsFragmentToPasswordListFragment()
                findNavController().navigateUp()
            }

            // Change AppBar text visibility if scrollview is scrolled
            appBarLayout.addOnOffsetChangedListener( object : CollapsingToolbarStateChangeListener() {
                override fun onStateChanged(appBarLayout: AppBarLayout?, currentState: State) {
                    if(currentState == State.COLLAPSED){
                        titleTextView.visibility = View.INVISIBLE
                        titleTextViewToolbar.visibility = View.VISIBLE
                    } else {
                        titleTextView.visibility = View.VISIBLE
                        titleTextViewToolbar.visibility = View.INVISIBLE
                    }

                }
            } )
            backButton.setOnClickListener {
                findNavController().navigateUp()
            }

        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard: ClipboardManager =
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("SimplePass", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(activity, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }

}