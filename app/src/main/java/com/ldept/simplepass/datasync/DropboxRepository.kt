package com.ldept.simplepass.datasync

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.provider.ContactsContract
import android.util.Log
import androidx.annotation.WorkerThread
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.*
import com.ldept.simplepass.database.PasswordDatabase
import com.ldept.simplepass.database.PasswordEntry
import com.ldept.simplepass.ui.SettingsActivity
import com.ldept.simplepass.util.DatabaseFileOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.*

class DropboxRepository(
    private val dropboxClient: DbxClientV2,
) {

    @SuppressLint("ApplySharedPref")
    fun getAccountInfo(sharedPrefs : SharedPreferences) {

        try {
            val email = dropboxClient.users().currentAccount.email
            sharedPrefs.edit().putString("logged-account", email).commit()
        } catch (e : Exception) {}


    }
    fun downloadDatabaseFile(tempFile : File, databaseFile: File, saltFile : File){
        if(doesBackupExist()) {
            try{
                val tempOutputStream = tempFile.outputStream()
                dropboxClient.files().downloadBuilder("/${SettingsActivity.DROPBOX_FILE_NAME}")
                    .download(tempOutputStream)
                tempOutputStream.close()
                val saltOutputStream = saltFile.outputStream()
                dropboxClient.files().downloadBuilder("/salt")
                    .download(saltOutputStream)
                saltOutputStream.close()
                val inputStream = tempFile.inputStream()
                val outputStream = databaseFile.outputStream()
                DatabaseFileOperations.copyFile(inputStream, outputStream)
                DatabaseFileOperations.removeWALAndSHM(databaseFile)
                tempFile.delete()
                PasswordDatabase.closeDatabase()
            } catch (e : Exception){
                Log.e("File download: ", e.message.toString())
            }
        }
    }
    fun uploadDatabaseFile(file : File, saltFile : File){
        try {
            val inputStream = FileInputStream(file)
            dropboxClient.files().uploadBuilder("/${SettingsActivity.DROPBOX_FILE_TEMP_NAME}")
                .withMode(WriteMode.OVERWRITE)
                .withClientModified(Date(file.lastModified()))
                .uploadAndFinish(inputStream)
            val saltInputStream = FileInputStream(saltFile)
            dropboxClient.files().uploadBuilder("/salt_tmp")
                .withMode(WriteMode.OVERWRITE)
                .withClientModified(Date(file.lastModified()))
                .uploadAndFinish(saltInputStream)
            if(doesBackupExist()) {
                dropboxClient.files().deleteV2("/${SettingsActivity.DROPBOX_FILE_NAME}")
                dropboxClient.files().deleteV2("/salt")
            }
            dropboxClient.files().moveV2(
                "/${SettingsActivity.DROPBOX_FILE_TEMP_NAME}",
                "/${SettingsActivity.DROPBOX_FILE_NAME}"
            )
            dropboxClient.files().moveV2(
                "/salt_tmp",
                "/salt"
            )

        } catch (e : Exception){
            Log.e("File upload: ", e.message.toString())
        }
    }
    suspend fun backupLastModified() : Date? = withContext(Dispatchers.IO){
        try {

            if (doesBackupExist()) {
                val metadata: FileMetadata = dropboxClient.files()
                    .getMetadata("/${SettingsActivity.DROPBOX_FILE_NAME}") as FileMetadata
                metadata.clientModified
            } else {
                null
            }
        } catch (e : Exception) { null }



    }

    private fun doesBackupExist() : Boolean{
        return try{
            dropboxClient.files().getMetadata("/${SettingsActivity.DROPBOX_FILE_NAME}")
            dropboxClient.files().getMetadata("/salt")
            true
        } catch (e : GetMetadataErrorException){
            if (e.message.toString().contains("{\".tag\":\"path\",\"path\":\"not_found\"}")) {
                false
            } else {
                throw e
            }
        }
    }
}