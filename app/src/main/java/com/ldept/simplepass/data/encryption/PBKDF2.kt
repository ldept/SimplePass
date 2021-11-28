package com.ldept.simplepass.data.encryption

import android.content.Context.MODE_PRIVATE
import com.ldept.simplepass.SimplePassApp
import net.sqlcipher.database.SQLiteDatabase
import java.io.FileNotFoundException
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PBKDF2 {

    private const val NUM_OF_ITERATIONS = 100000
    private const val SIZE_OF_KEY = 64 * Byte.SIZE_BITS

    // Takes in user-provided password and salt and returns derived key along with used salt
    // (different than the provided one if provided salt was an empty string
    private fun generatePassword(userPassword: String, userSalt: String): Pair<String, String> {
        val salt = getSaltFromString(userSalt)
        val keySpec = PBEKeySpec(userPassword.toCharArray(), salt, NUM_OF_ITERATIONS, SIZE_OF_KEY)
        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA512")
        val keyByteArray = secretKeyFactory.generateSecret(keySpec).encoded
        val password = Base64.getEncoder().encodeToString(keyByteArray)
        val saltString = Base64.getEncoder().encodeToString(salt)
        return Pair(password, saltString)
    }

    private fun getSaltFromString(salt: String): ByteArray {
        return if (salt == "") {
            val secureRandom = SecureRandom()
            val newSalt = ByteArray(SIZE_OF_KEY)
            secureRandom.nextBytes(newSalt)
            newSalt
        } else {
            Base64.getDecoder().decode(salt)
        }
    }


    fun checkPassword(
        userPassword: String,
    ) : String {
        val sharedPrefs = SimplePassApp.context.getSharedPreferences(
            SimplePassApp.context.packageName,
            MODE_PRIVATE
        )
        val userSalt: String = sharedPrefs.getString("pbkdf2-salt", "").toString()
        val pbkdf2Pair = generatePassword(userPassword, userSalt)
        val password = pbkdf2Pair.first
        if (!sharedPrefs.contains("pbkdf2-salt")) {
            sharedPrefs.edit().putString("pbkdf2-salt", pbkdf2Pair.second).apply()
        }

        val dbFile = SimplePassApp.dbFile
        if (dbFile != null && dbFile.exists())
            SQLiteDatabase.openDatabase(
                dbFile.path,
                password,
                null,
                0
            )
        else
            throw FileNotFoundException("Database file doesn't exist")

        return password

    }


}