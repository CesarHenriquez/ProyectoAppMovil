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
        val EMAIL: Preferences.Key<String> = stringPreferencesKey("email")
        val ROLE: Preferences.Key<String> = stringPreferencesKey("role")
    }

    // Flujo del email de sesión (o null si no hay).
    val emailFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[Keys.EMAIL] }

    // Flujo del rol persistido (o null si no hay).
    val roleFlow: Flow<UserRole?> = dataStore.data.map { prefs ->
        prefs[Keys.ROLE]?.let { saved ->
            runCatching { UserRole.valueOf(saved) }.getOrNull()
        }
    }

    // Guarda email + rol.
    suspend fun saveSession(email: String, role: UserRole) {
        dataStore.edit { prefs ->
            prefs[Keys.EMAIL] = email
            prefs[Keys.ROLE] = role.name
        }
    }

    // Limpia la sesión.
    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}