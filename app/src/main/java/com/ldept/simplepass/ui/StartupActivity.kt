package com.ldept.simplepass.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.ldept.simplepass.R
import net.sqlcipher.database.SQLiteDatabase

class StartupActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PASSWORD = "com.ldept.simplepass.ui.StartupActivity.EXTRA_PASSWORD"
    }
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)


        SQLiteDatabase.loadLibs(this)

        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.startup_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
    }
}