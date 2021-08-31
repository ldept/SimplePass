package com.ldept.simplepass.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.DateFormat

@Parcelize
@Entity(tableName = "password_entry_table")
data class PasswordEntry (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val login: String,
    val email: String,
    val password: String,
    val lastModified : Long = System.currentTimeMillis()
) : Parcelable {
    val lastModifiedFormatted : String
        get() = DateFormat.getDateInstance().format(lastModified)
}