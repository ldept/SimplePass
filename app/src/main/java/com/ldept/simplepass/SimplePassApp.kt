package com.ldept.simplepass

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.ldept.simplepass.data.PreferencesRepository

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class SimplePassApp : Application() {
    init {
        instance = this
    }
    companion object {
        var instance: SimplePassApp? = null
        val context: Context by lazy {
            instance!!.applicationContext
        }

        val preferencesRepository by lazy {
            PreferencesRepository(context.dataStore)
        }
    }
}
