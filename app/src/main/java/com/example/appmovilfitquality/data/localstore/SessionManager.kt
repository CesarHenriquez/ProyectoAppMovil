package com.example.appmovilfitquality.data.localstore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.appmovilfitquality.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.sessionDataStore by preferencesDataStore(name = "session_prefs")

class SessionManager(context: Context) {

    private val appContext = context.applicationContext
    private val dataStore = appContext.sessionDataStore

    private object Keys {

        val USER_ID: Preferences.Key<String> = stringPreferencesKey("user_id")
        val EMAIL: Preferences.Key<String> = stringPreferencesKey("email")
        val ROLE: Preferences.Key<String> = stringPreferencesKey("role")
        val TOKEN: Preferences.Key<String> = stringPreferencesKey("auth_token")
    }


    val userIdFlow: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[Keys.USER_ID]?.toLongOrNull()
    }


    val emailFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[Keys.EMAIL] }


    val roleFlow: Flow<UserRole?> = dataStore.data.map { prefs ->
        prefs[Keys.ROLE]?.let { saved ->
            runCatching { UserRole.valueOf(saved) }.getOrNull()
        }
    }


    val tokenFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[Keys.TOKEN] }


    suspend fun saveSession(userId: Int, email: String, role: UserRole, token: String) {
        dataStore.edit { prefs ->

            prefs[Keys.USER_ID] = userId.toString()
            prefs[Keys.EMAIL] = email
            prefs[Keys.ROLE] = role.name
            prefs[Keys.TOKEN] = token
        }
    }


    suspend fun updateSessionRole(role: UserRole) {
        dataStore.edit { prefs ->
            prefs[Keys.ROLE] = role.name
        }
    }


    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}