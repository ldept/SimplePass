package com.ldept.simplepass

import android.app.Application

class SimplePassApp : Application() {
    init {
        instance = this
    }
    companion object {
        private var instance: SimplePassApp? = null
        val context by lazy {
            instance!!.applicationContext
        }
        val dbFile by lazy {
            instance?.getDatabasePath("password_database_encrypted")
        }
    }


    override fun onCreate() {
        super.onCreate()
    }
}
