package com.ldept.simplepass.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ldept.simplepass.R

class StartupActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PASSWORD = "com.ldept.simplepass.ui.StartupActivity.EXTRA_PASSWORD"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
    }
}