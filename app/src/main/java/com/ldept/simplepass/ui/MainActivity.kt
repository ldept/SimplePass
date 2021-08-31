package com.ldept.simplepass.ui



import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ldept.simplepass.*
import com.ldept.simplepass.database.*
import com.ldept.simplepass.util.CollapsingToolbarStateChangeListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.system.exitProcess


class MainActivity
    : AppCompatActivity(),
      SearchView.OnQueryTextListener,
      PasswordListAdapter.OnItemClickListener {

    companion object{
        const val ADD_PASSWORD_REQUEST = 1
        const val UPDATE_PASSWORD_REQUEST = 2
        const val SETTINGS_BACKUP_RESTORED_REQUEST = 3
    }
    private lateinit var passwordViewModel: PasswordViewModel
    private lateinit var adapter : PasswordListAdapter
    private lateinit var searchView : SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val appBarLayout : AppBarLayout = findViewById(R.id.app_bar_layout)
        val titleTextView : TextView = findViewById(R.id.title_text_view_toolbar)

        var shouldTitleTextViewBeVisible = true
        titleTextView.visibility = View.GONE


        // Change AppBar text visibility if recyclerview is scrolled
        appBarLayout.addOnOffsetChangedListener( object : CollapsingToolbarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, currentState: State) {
                if(currentState == State.COLLAPSED){
                    titleTextView.visibility = View.VISIBLE
                } else {
                    if(shouldTitleTextViewBeVisible)
                        titleTextView.visibility = View.GONE
                }

            }
        } )

        searchView = findViewById(R.id.search_bar_searchView)
        searchView.setOnQueryTextListener(this)
        searchView.setOnSearchClickListener {
            shouldTitleTextViewBeVisible = false
            titleTextView.visibility = View.GONE

        }
        searchView.setOnCloseListener( SearchView.OnCloseListener {
            shouldTitleTextViewBeVisible = true
            titleTextView.visibility = View.VISIBLE
            searchView.onActionViewCollapsed()
            true
        })



        // Database access
        val recyclerView : RecyclerView = findViewById(R.id.recyclerview)
        adapter = PasswordListAdapter(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        val password = intent.getStringExtra(LoginActivity.EXTRA_PASSWORD).toString()
        val passwordDao = PasswordDatabase.getDatabase(
            this,
            password
        ).passwordEntryDAO()

        val repository  = PasswordRepository(passwordDao)
        val viewModelFactory = PasswordViewModelFactory(repository)
        passwordViewModel = ViewModelProvider(this, viewModelFactory).get(PasswordViewModel::class.java)
        passwordViewModel.allPasswords.observe(this, Observer { passwords ->
            passwords?.let{
                adapter.setPasswords(passwords)
            }
        })


        val fab : FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, AddUpdatePasswordActivity::class.java)
            startActivityForResult(intent, ADD_PASSWORD_REQUEST)
        }

        val settingsButton : ImageButton = findViewById(R.id.settings_button)
        settingsButton.setOnClickListener{
            // Create a checkpoint to move every database change from .wal and .shm  to main .db file
            passwordViewModel.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
            val intent = Intent( this@MainActivity, SettingsActivity::class.java)
            startActivityForResult(intent, SETTINGS_BACKUP_RESTORED_REQUEST)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

    }

    private fun setNewPassword(){
        val newPassword = intent.getStringExtra(AuthenticationSettingsActivity.NEW_PASSWORD_EXTRA)
        passwordViewModel.changePassword(SimpleSQLiteQuery("pragma rekey='${newPassword}';"))
    }

    override fun onResume() {
        super.onResume()
        if (intent.hasExtra(AuthenticationSettingsActivity.NEW_PASSWORD_EXTRA)){
            setNewPassword()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PASSWORD_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                val passwordEntry = data.getParcelableExtra<PasswordEntry>(AddUpdatePasswordActivity.EXTRA_PASSWORD_ENTRY) as PasswordEntry
                passwordViewModel.insert(passwordEntry)
                Toast.makeText(this, getString(R.string.new_password_added), Toast.LENGTH_LONG).show()
            } catch (exception : Exception){
                Toast.makeText(this, exception.toString() , Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == UPDATE_PASSWORD_REQUEST && resultCode == RESULT_OK && data != null){
                try {
                    val passwordEntry = data.getParcelableExtra<PasswordEntry>(
                        AddUpdatePasswordActivity.EXTRA_PASSWORD_ENTRY
                    ) as PasswordEntry
                    passwordViewModel.update(passwordEntry)
                    Toast.makeText(this, getString(R.string.password_updated), Toast.LENGTH_LONG).show()
                } catch (exception: Exception) {
                    Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
                }

        } else if (requestCode == UPDATE_PASSWORD_REQUEST && resultCode == AddUpdatePasswordActivity.RESULT_DELETE && data != null){
                try {
                    val passwordEntry = data.getParcelableExtra<PasswordEntry>(
                        AddUpdatePasswordActivity.EXTRA_PASSWORD_ENTRY
                    ) as PasswordEntry
                    passwordViewModel.delete(passwordEntry)
                    Toast.makeText(this, getString(R.string.password_deleted), Toast.LENGTH_LONG).show()
                } catch (exception: Exception) {
                    Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
                }

        } else if (requestCode == SETTINGS_BACKUP_RESTORED_REQUEST && resultCode == SettingsActivity.RESULT_BACKUP_RESTORED){
            // Recreate (restart) current activity to reload the database
            finish()
            val resetIntent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(resetIntent)

        }
    }


    override fun onItemClick(password: PasswordEntry) {
        val intent = Intent(this@MainActivity, AddUpdatePasswordActivity::class.java)
        intent.putExtra(AddUpdatePasswordActivity.EXTRA_PASSWORD_ENTRY, password)
        startActivityForResult(intent, UPDATE_PASSWORD_REQUEST)
    }

    override fun onBackPressed() {
        if(searchView.query.isNotEmpty()){
            searchView.setQuery("",false)
            searchView.clearFocus()
            searchView.isIconified = true
        } else if(!searchView.isIconified){
            searchView.isIconified = true
        } else
            super.onBackPressed()
    }
    override fun onQueryTextSubmit(query: String?): Boolean {
        searchView.clearFocus()
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(newText != null){
            searchPasswords(newText)
        }
        return true
    }

    private fun searchPasswords(query : String){
        val searchQuery = "%$query%"
        passwordViewModel.searchPasswords(searchQuery).observe(this, Observer { passwords ->
            passwords.let {
                adapter.setPasswords(passwords)
            }
        })
    }


}