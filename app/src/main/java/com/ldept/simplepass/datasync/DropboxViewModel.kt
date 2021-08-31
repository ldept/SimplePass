package com.ldept.simplepass.datasync

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.ldept.simplepass.database.PasswordEntry
import com.ldept.simplepass.database.PasswordRepository
import com.ldept.simplepass.database.PasswordViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.IllegalArgumentException
import java.time.Instant
import java.util.*

class DropboxViewModel(private val repository: DropboxRepository) : ViewModel() {
    val lastModified : LiveData<Date>
        get() = _lastModified
    private val _lastModified = MutableLiveData<Date>()



    val hasDownloadOrUploadFinished : LiveData<Boolean>
        get() = _hasDownloadOrUploadFinished
    private val _hasDownloadOrUploadFinished = MutableLiveData<Boolean>()


    init {
        _lastModified.value = Date(0L)
        _hasDownloadOrUploadFinished.value = false
    }



    fun getAccountInfo(sharedPrefs : SharedPreferences) = CoroutineScope(Dispatchers.IO).launch {
        repository.getAccountInfo(sharedPrefs)
    }

    fun uploadDatabaseFile(context: Context, file : File) = CoroutineScope(Dispatchers.IO).launch {
        val salt = context.getSharedPreferences(context.packageName, MODE_PRIVATE).getString("pbkdf2-salt","")
        val saltFile = File(context.filesDir, "salt")
        saltFile.printWriter().use { out ->
            out.println(salt)
        }
        repository.uploadDatabaseFile(file, saltFile)
        _hasDownloadOrUploadFinished.postValue(true)
    }.also { backupLastModified() }

    fun downloadDatabaseFile(context: Context) = CoroutineScope(Dispatchers.IO).launch {
        val tempFile = File(context.filesDir, "tmp.db")
        val databaseFile = context.getDatabasePath("password_database_encrypted")
        val saltFile = File(context.filesDir, "salt")
        repository.downloadDatabaseFile(tempFile, databaseFile, saltFile)
        _hasDownloadOrUploadFinished.postValue(true)
    }

    fun backupLastModified() = CoroutineScope(Dispatchers.IO).launch {
        val date = repository.backupLastModified()
        if(date != null)
            _lastModified.postValue(date!!)
    }


    fun downloadOrUploadFinishHandled() {
        _hasDownloadOrUploadFinished.value = false
    }
}

class DropboxViewModelFactory(private val repository: DropboxRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(DropboxViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return  DropboxViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}