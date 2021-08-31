package com.ldept.simplepass.util

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.ldept.simplepass.ui.AuthenticationSettingsActivity
import com.ldept.simplepass.ui.LoginActivity
import com.ldept.simplepass.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PBKDF2 {

    private const val NUM_OF_ITERATIONS = 100000
    private const val SIZE_OF_KEY = 64 * Byte.SIZE_BITS

    // Takes in user-provided password and salt and returns derived key along with used salt
    // (different than the provided one if provided salt was an empty string
    private fun generatePassword(userPassword : String, userSalt : String) : Pair<String,String>{
        val salt = getSalt(userSalt)
        val keySpec = PBEKeySpec(userPassword.toCharArray(),salt, NUM_OF_ITERATIONS, SIZE_OF_KEY)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA512")
        val keyByteArray = secretKeyFactory.generateSecret(keySpec).encoded
        val password = Base64.getEncoder().encodeToString(keyByteArray)
        val saltString = Base64.getEncoder().encodeToString(salt)
        return Pair(password, saltString)
    }

    private fun getSalt(salt : String) : ByteArray {
        return if(salt == ""){
            val secureRandom = SecureRandom()
            val newSalt = ByteArray(SIZE_OF_KEY)
            secureRandom.nextBytes(newSalt)
            newSalt
        } else {
            Base64.getDecoder().decode(salt)
        }
    }


    suspend fun checkOrCreatePassword(userPassword: String,
                      callingActivity : AppCompatActivity,
                      sharedPrefs : SharedPreferences,
                      createNewPassword : Boolean,
                      onFailure : () -> Unit){
        val userSalt: String = sharedPrefs.getString("pbkdf2-salt", "").toString()
        val pbkdf2Pair = generatePassword(userPassword, userSalt)
        val password = pbkdf2Pair.first
        if (!sharedPrefs.contains("pbkdf2-salt")) {
            sharedPrefs.edit().putString("pbkdf2-salt", pbkdf2Pair.second).apply()
        }
        // Can't do anything related to the UI (which may be done in onFailure) on a worker thread
        // So opening the database and errors which may happen need to be handled on the main thread
        withContext(Dispatchers.Main) {
            try {
                val dbFile = callingActivity.getDatabasePath("password_database_encrypted")
                if (dbFile.exists() && !createNewPassword)
                    SQLiteDatabase.openDatabase(
                        dbFile.path,
                        password,
                        null,
                        0
                    )

                val intent = Intent(callingActivity, MainActivity::class.java)
                if(createNewPassword)
                    intent.putExtra(AuthenticationSettingsActivity.NEW_PASSWORD_EXTRA, password)
                else
                    intent.putExtra(LoginActivity.EXTRA_PASSWORD, password)
                callingActivity.startActivity(intent)
                callingActivity.finish()

            } catch (exception : Exception){
                when(exception){
                    is SQLiteException -> onFailure()
                }
            }
        }
    }



}