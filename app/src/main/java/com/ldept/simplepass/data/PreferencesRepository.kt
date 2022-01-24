package com.ldept.simplepass.data


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ldept.simplepass.data.PreferencesRepository.PreferencesKeys.DB_FILE_PATH
import com.ldept.simplepass.data.PreferencesRepository.PreferencesKeys.IS_BIOMETRICS_ENABLED
import com.ldept.simplepass.data.PreferencesRepository.PreferencesKeys.IS_FIRST_LAUNCH
import com.ldept.simplepass.data.PreferencesRepository.PreferencesKeys.PBKDF2_SALT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class UserPreferences(
    val isFirstLaunch: Boolean,
    val isBiometricsEnabled: Boolean,
    val pbkdf2Salt: String,
    val dbFilePath: String,
)

class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            throw exception
        }
        .map { preferences ->
            mapUserPreferences(preferences)
        }

    suspend fun setPBKDF2Salt(pbkdf2Salt: String) =
        dataStore.edit { preferences ->
            preferences[PBKDF2_SALT] = pbkdf2Salt
        }

    suspend fun setDbFilePath(dbFilePath: String) =
        dataStore.edit { preferences ->
            preferences[DB_FILE_PATH] = dbFilePath
        }

    suspend fun setIsFirstLaunch(isFirstLaunch: Boolean) =
        dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = isFirstLaunch
        }

    suspend fun setIsBiometricsEnabled(isBiometricsEnabled: Boolean) =
        dataStore.edit { preferences ->
            preferences[IS_BIOMETRICS_ENABLED] = isBiometricsEnabled
        }


    private fun mapUserPreferences(preferences: Preferences) : UserPreferences{
        val pbkdf2Salt = preferences[PBKDF2_SALT] ?: ""
        // TODO: Change dbFilePath to String!
        val dbFilePath = preferences[DB_FILE_PATH] ?: ""
        val isFirstLaunch = preferences[IS_FIRST_LAUNCH] ?: true
        val isBiometricsEnabled = preferences[IS_BIOMETRICS_ENABLED] ?: false
        return UserPreferences(
            isFirstLaunch = isFirstLaunch,
            isBiometricsEnabled = isBiometricsEnabled,
            pbkdf2Salt = pbkdf2Salt,
            dbFilePath = dbFilePath,
        )
    }

    private object PreferencesKeys {
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val IS_BIOMETRICS_ENABLED = booleanPreferencesKey("is_biometrics_enabled")
        val DROPBOX_AUTH_TOKEN = stringPreferencesKey("dropbox_auth_token")
        val PBKDF2_SALT = stringPreferencesKey("pbkdf2_salt")
        val DB_FILE_PATH = stringPreferencesKey("db_file_path")
    }
}