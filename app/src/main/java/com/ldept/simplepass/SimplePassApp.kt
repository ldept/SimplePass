package com.ldept.simplepass

import android.app.Application
import android.content.Context

class SimplePassApp : Application() {
    init {
        instance = this
    }
    companion object {
        var instance: SimplePassApp? = null
        val context: Context by lazy {
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
