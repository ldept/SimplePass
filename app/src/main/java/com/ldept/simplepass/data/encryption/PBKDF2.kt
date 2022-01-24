package com.ldept.simplepass.data.encryption

import android.content.Context.MODE_PRIVATE
import com.ldept.simplepass.SimplePassApp
import kotlinx.coroutines.flow.collect
import net.sqlcipher.database.SQLiteDatabase
import java.io.File
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


    fun checkDatabasePassword(
        userPassword: String,
        userSalt: String,
        dbFilePath: String,
    ): Pair<String, String> {

        val (password, salt) = generatePassword(userPassword, userSalt)
        // Try opening the database with provided password
        SQLiteDatabase.openDatabase(
            dbFilePath,
            password,
            null,
            0
        )

        return Pair(password, salt)

    }

    fun getNewPassword(userPassword: String, userSalt: String): String =
        generatePassword(userPassword, userSalt).first


}