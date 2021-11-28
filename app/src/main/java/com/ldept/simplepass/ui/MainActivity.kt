package com.ldept.simplepass.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.ldept.simplepass.R
import com.ldept.simplepass.data.PasswordDatabase
import com.ldept.simplepass.data.PasswordDatabaseRepository
import com.ldept.simplepass.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var database: PasswordDatabase
    lateinit var repository: PasswordDatabaseRepository

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Password is checked before moving to MainActivity, so it shouldn't be empty
        val password: String = intent.getStringExtra(StartupActivity.EXTRA_PASSWORD).toString()
        database = PasswordDatabase.getDatabase(this, password)
        repository = PasswordDatabaseRepository(database.passwordEntryDAO())

        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()



    }
}