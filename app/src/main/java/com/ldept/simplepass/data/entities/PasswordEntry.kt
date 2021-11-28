package com.ldept.simplepass.data.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
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
