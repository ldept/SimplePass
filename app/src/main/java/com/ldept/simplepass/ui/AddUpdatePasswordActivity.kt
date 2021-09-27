package com.ldept.simplepass.ui

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.ldept.simplepass.database.PasswordEntry
import com.ldept.simplepass.R
import com.ldept.simplepass.databinding.ActivityNewPasswordBinding


class AddUpdatePasswordActivity : AppCompatActivity() {
    companion object{
        const val RESULT_DELETE : Int = 3
        const val EXTRA_PASSWORD_ENTRY = "package com.ldept.simplepass.EXTRA_PASSWORD_ENTRY"
    }

    private lateinit var binding : ActivityNewPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE)
        binding = ActivityNewPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val editTextName : EditText = binding.nameAddEditText
        val editTextLogin : EditText = binding.loginAddEditText
        val editTextPassword : EditText = binding.passwordAddEditText
        val editTextEmail : EditText = binding.emailAddEditText
        val saveButton : Button = binding.passwordAddSaveButton



        if(intent.hasExtra(EXTRA_PASSWORD_ENTRY)){
            val passwordEntry = intent.getParcelableExtra<PasswordEntry>(EXTRA_PASSWORD_ENTRY) as PasswordEntry
            editTextName.setText(passwordEntry.name)
            editTextLogin.setText(passwordEntry.login)
            editTextPassword.setText(passwordEntry.password)
            editTextEmail.setText(passwordEntry.email)

            val copyLoginButton : ImageButton = binding.copyLoginButton
            val copyEmailButton : ImageButton = binding.copyEmailButton
            val copyPasswordButton : ImageButton = binding.copyPasswordButton
            copyEmailButton.visibility = View.VISIBLE
            copyLoginButton.visibility = View.VISIBLE
            copyPasswordButton.visibility = View.VISIBLE
            val clipboard: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            copyLoginButton.setOnClickListener {
                val clip = ClipData.newPlainText("login", editTextLogin.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
            }

            copyEmailButton.setOnClickListener{
                val clip = ClipData.newPlainText("login", editTextEmail.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
            }
            copyPasswordButton.setOnClickListener {
                val clip = ClipData.newPlainText("login", editTextPassword.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
            }
        }

        saveButton.setOnClickListener{
            val replyIntent = Intent()

            val nameText = editTextName.text.toString()
            val loginText = editTextLogin.text.toString()
            val passwordText = editTextPassword.text.toString()
            val emailText = editTextEmail.text.toString()

            if(nameText.isEmpty() or passwordText.isEmpty()){
                Toast.makeText(this, getString(R.string.provide_name_and_password_toast), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            var id = 0
            if (intent.hasExtra(EXTRA_PASSWORD_ENTRY))
                id = (intent.extras?.get(EXTRA_PASSWORD_ENTRY) as PasswordEntry).id

            val passwordEntry = PasswordEntry(id, nameText,loginText,emailText,passwordText)
            replyIntent.putExtra(EXTRA_PASSWORD_ENTRY, passwordEntry)

            setResult(RESULT_OK, replyIntent)
            finish()

        }
    }

    private fun deleteItem(){
        val replyIntent = Intent()
        val passwordEntry = intent.getParcelableExtra<PasswordEntry>(EXTRA_PASSWORD_ENTRY) as PasswordEntry
        replyIntent.putExtra(EXTRA_PASSWORD_ENTRY, passwordEntry)
        setResult(RESULT_DELETE, replyIntent)
        finish()
    }

    private fun deleteDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.password_delete_dialog_title))
        builder.setMessage(getString(R.string.password_delete_dialog_message))
        builder.setPositiveButton(getString(R.string.yes), DialogInterface.OnClickListener { dialog, _ ->
            deleteItem()
            dialog.dismiss()
        })
        builder.setNegativeButton(getString(R.string.no), DialogInterface.OnClickListener{ dialog, _ ->
            dialog.dismiss()
        })
        val alertDialog = builder.create()

        alertDialog.show()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(intent.hasExtra(EXTRA_PASSWORD_ENTRY)) {
            menuInflater.inflate(R.menu.add_update_password_menu, menu)
            title = getString(R.string.AddUpdatePasswordActivityTitleUpdate)
        } else {
            title = getString(R.string.AddUpdatePasswordActivityTitleAdd)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.menu_action_delete_password -> {
                deleteDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


}