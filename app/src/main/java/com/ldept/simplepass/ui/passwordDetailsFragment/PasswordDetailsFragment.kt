package com.ldept.simplepass.ui.passwordDetailsFragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.ldept.simplepass.R
import com.ldept.simplepass.data.entities.PasswordEntry
import com.ldept.simplepass.databinding.FragmentPasswordDetailsBinding
import com.ldept.simplepass.ui.MainActivity
import com.ldept.simplepass.ui.util.CollapsingToolbarStateChangeListener
import com.ldept.simplepass.ui.util.SplashScreenAnimation
import com.ldept.simplepass.ui.util.SplashScreenAnimation.crossfade

class PasswordDetailsFragment : Fragment() {

    private val args: PasswordDetailsFragmentArgs by navArgs()
    private lateinit var viewModel: PasswordDetailsViewModel

    private enum class Content {
        EDIT_CONTENT, NON_EDIT_CONTENT
    }

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
        viewModel = ViewModelProvider(this, PasswordDetailsViewModelFactory(repository))
            .get(PasswordDetailsViewModel::class.java)

        // Outer layouts for edit and nonEdit content (include ids)
        val editContent = binding.editContent
        val nonEditContent = binding.nonEditContent

        binding.apply {

            /*
                When a passwordEntry was passed to the fragment we want to show the nonEditContent
                first. Else We show the editContent first.
            */
            passwordEntryArg?.also { passwordEntry ->

                showContent(this, Content.NON_EDIT_CONTENT)

                nonEditContent.apply {
                    nameTextview.text = passwordEntry.name
                    loginTextview.text = passwordEntry.login
                    passwordTextview.text = passwordEntry.password
                    emailTextview.text = passwordEntry.email
                    lastModifiedTextview.text = passwordEntry.lastModifiedFormatted

                    copyPasswordButton.setOnClickListener {
                        val password = passwordTextview.text.toString()
                        copyToClipboard(password)
                    }
                    copyLoginButton.setOnClickListener {
                        val login = loginTextview.text.toString()
                        copyToClipboard(login)
                    }
                    copyEmailButton.setOnClickListener {
                        val email = emailTextview.text.toString()
                        copyToClipboard(email)
                    }
                }
            } ?: run {
                showContent(this, Content.EDIT_CONTENT)
                deleteButton.isVisible = false
                titleTextView.text = getString(R.string.AddUpdatePasswordActivityTitleAdd)
                titleTextViewToolbar.text = getString(R.string.AddUpdatePasswordActivityTitleAdd)
            }

            passwordAddSaveEditButton.setOnClickListener {
                /*
                    If editContent is visible we are either adding(inserting)
                    or updating the passwordEntry
                */
                if (editContent.editContent.isVisible) {
                    editContent.apply {
                        // TODO: If name and login or password are empty then show a Toast
                        val passwordEntryToSave = PasswordEntry(
                            id = passwordEntryArg?.id ?: 0,
                            name = nameAddEditText.text.toString(),
                            login = loginAddEditText.text.toString(),
                            email = emailAddEditText.text.toString(),
                            password = passwordAddEditText.text.toString()
                        )
                        if (passwordEntryArg == null)
                            viewModel.insert(passwordEntryToSave)
                        else
                            viewModel.update(passwordEntryToSave)

                        findNavController().navigateUp()
                    }
                } else {
                    /*
                        NonEditContent was visible, so after the click on the edit fab we need to
                        change the image on the fab and the visibility of views
                    */
                    passwordEntryArg?.let { passwordEntry ->
                        editContent.apply {
                            nameAddEditText.setText(passwordEntry.name)
                            loginAddEditText.setText(passwordEntry.login)
                            passwordAddEditText.setText(passwordEntry.password)
                            emailAddEditText.setText(passwordEntry.email)
                        }
                    }

                    showContent(this, Content.EDIT_CONTENT)
                }
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
            backButton.setOnClickListener {
                findNavController().navigateUp()
            }
            deleteButton.setOnClickListener {
                passwordEntryArg?.let {
                    showDeleteDialog(requireContext(), it)
                }
            }

        }
    }

    private fun showContent(binding: FragmentPasswordDetailsBinding, content: Content) {
        when (content) {
            Content.NON_EDIT_CONTENT ->
                binding.apply {
                    nonEditContent.nonEditContent.isVisible = true
                    editContent.editContent.isVisible = false
                    passwordAddSaveEditButton.setImageResource(R.drawable.ic_baseline_edit_24)
                }
            Content.EDIT_CONTENT ->
                binding.apply {
                    crossfade(nonEditContent.nonEditContent, editContent.editContent)
                    passwordAddSaveEditButton.setImageResource(R.drawable.ic_baseline_check_24)
                }
        }

    }

    private fun showDeleteDialog(context: Context, passwordEntry: PasswordEntry) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.password_delete_dialog_title))
        builder.setMessage(getString(R.string.password_delete_dialog_message))
        builder.setPositiveButton(
            getString(R.string.yes),
            DialogInterface.OnClickListener { dialog, _ ->
                viewModel.delete(passwordEntry)
                dialog.dismiss()
                findNavController().navigateUp()
            })
        builder.setNegativeButton(
            getString(R.string.no),
            DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            })
        val alertDialog = builder.create()

        alertDialog.show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard: ClipboardManager =
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("SimplePass", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(activity, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
    }

}